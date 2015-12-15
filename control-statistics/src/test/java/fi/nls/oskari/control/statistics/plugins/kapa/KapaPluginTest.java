package fi.nls.oskari.control.statistics.plugins.kapa;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.IndicatorValue;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOHelper.class)
public class KapaPluginTest {

    final private StatisticalDatasourcePluginManager manager = new StatisticalDatasourcePluginManager();
    private static String testIndicatorsResponse = ResourceHelper.readStringResource("KapaIndicators.json",
            KapaPluginTest.class);
    private static String testIndicatorDataResponse = ResourceHelper.readStringResource("KapaIndicatorData.json",
            KapaPluginTest.class);
    private static String url = "";

    @BeforeClass
    public static void init() throws IOException {
        PropertyUtil.loadProperties("/oskari-ext.properties");
        PowerMockito.mockStatic(IOHelper.class);
        when(IOHelper.getConnection(any(String.class))).then(new Answer<HttpURLConnection>() {
            @Override
            public HttpURLConnection answer(InvocationOnMock invocation) throws Throwable {
                url = invocation.getArguments()[0].toString();
                return (HttpURLConnection) new HttpURLConnection(null) {
                    @SuppressWarnings("deprecation")
                    @Override
                    public InputStream getInputStream() {
                        return new StringBufferInputStream(testIndicatorsResponse);
                    }
                    @Override
                    public void disconnect() {
                    }

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public void connect() throws IOException {
                    }
                };
            }
        });
        when(IOHelper.readString(any(InputStream.class), any(String.class))).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (url.startsWith("file:///opt/oskari/mockresponses/1.0/indicators/")) {
                    // Querying single indicator.
                    return testIndicatorDataResponse;
                } else {
                    // Querying all indicators.
                    return testIndicatorsResponse;
                }
            }
            
        });
    }
    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test(timeout=120000)
    public void testKapaPlugin()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        manager.reset();
        manager.registerPlugin("fi.nls.oskari.control.statistics.plugins.kapa.KapaStatisticalDatasourcePlugin", null);
        assertEquals(1, manager.getPlugins().size());
        StatisticalDatasourcePlugin kapaPlugin = null;
        Iterator<StatisticalDatasourcePlugin> pluginsIterator = manager.getPlugins().iterator();
        while (kapaPlugin == null && pluginsIterator.hasNext()) {
            StatisticalDatasourcePlugin nextPlugin = pluginsIterator.next();
            if (nextPlugin instanceof KapaStatisticalDatasourcePlugin) {
                kapaPlugin = nextPlugin;
            }
        }
        assertNotNull("KaPa plugin was not found.", kapaPlugin);
        
        // Getting indicators.
        List<? extends StatisticalIndicator> indicators = kapaPlugin.getIndicators(null);
        assertTrue("Indicators result was too small.", indicators.size() > 1);
        
        StatisticalIndicatorSelectors selectors = indicators.get(0).getSelectors();
        List<StatisticalIndicatorSelector> allSelectors = selectors.getSelectors();
        for (StatisticalIndicatorSelector selector : allSelectors) {
            // Selecting the first allowed value for each selector to define a proper selector.
            selector.setValue(selector.getAllowedValues().iterator().next());
        }
        Map<String, IndicatorValue> indicatorValues = indicators.get(0).getLayers().get(0).getIndicatorValues(selectors);
        assertNotNull("Indicator values response was null.", indicatorValues);
        assertTrue("IndicatorValues result was too small: " + String.valueOf(indicatorValues), indicatorValues.size() > 2);
    }
}

