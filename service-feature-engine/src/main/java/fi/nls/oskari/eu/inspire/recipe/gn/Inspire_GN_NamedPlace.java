package fi.nls.oskari.eu.inspire.recipe.gn;


import java.io.IOException;

import fi.nls.oskari.eu.inspire.gmlas.geographicalnames.NamedPlace;
import fi.nls.oskari.fe.input.format.gml.recipe.JacksonParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.schema.XSDDatatype;

public class Inspire_GN_NamedPlace extends JacksonParserRecipe {

    @Override
    public void parse() throws IOException {

        getGeometryDeserializer().mapGeometryTypes(
                "http://www.opengis.net/gml/3.2", "Point", "MultiPoint");

        FeatureOutputContext outputContext = new FeatureOutputContext(
                NamedPlace.QN);

        final Resource geom = outputContext.addDefaultGeometryProperty();
        final Resource gn = outputContext.addOutputProperty("name");
        final Resource beginLifespanVersion = outputContext
                .addOutputStringProperty("beginLifespanVersion");
        final Resource inspireId = outputContext.addOutputProperty("inspireId");
        final Resource endLifespanVersion = outputContext
                .addOutputStringProperty("endLifespanVersion");

        OutputFeature<NamedPlace> outputFeature = new OutputFeature<NamedPlace>(
                outputContext);

        InputFeature<NamedPlace> iter = new InputFeature<NamedPlace>(
                NamedPlace.QN, NamedPlace.class);

        while (iter.hasNext()) {
            NamedPlace namedPlace = iter.next();
            Resource output_ID = outputContext.uniqueId(namedPlace.id);

            outputFeature.setFeature(namedPlace).setId(output_ID);

            outputFeature.addGeometryProperty(geom,
                    namedPlace.geometry.geometry);

            outputFeature
                    .addProperty(gn, namedPlace.name)
                    .addProperty(beginLifespanVersion,
                            namedPlace.beginLifespanVersion)
                    .addProperty(inspireId, namedPlace.inspireId)
                    .addProperty(endLifespanVersion,
                            namedPlace.endLifespanVersion);

            outputFeature.build();

        }

    }

}
