package fi.nls.oskari.control.users.service;

import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.service.db.BaseIbatisService;

public class IbatisEmailService  extends BaseIbatisService<Email>{

	@Override
	protected String getNameSpace() {
		return "Emails";
	}

	public Long addEmail(Email email) {
        return queryForObject(getNameSpace() + ".addEmail", email);
    }
	
	public Email findByToken(String uuid) {
        return queryForObject(getNameSpace() + ".findByToken", uuid);
    }
	
	public String findUsernameForEmail(String email) {
		return (String) queryForRawObject(getNameSpace() + ".findUsernameForEmail", email);
	}
	
	public String findUsernameForLogin(String username) {
		return (String) queryForRawObject(getNameSpace() + ".findUsernameForLogin", username);
	}
	
	public void deleteEmailToken(String uuid) {
		Map<String, String> params = new HashMap<String, String>(2);
        params.put("uuid", uuid);
	    delete(getNameSpace() + ".deleteEmailToken", params);
	}
	
	public String findEmailForUsername(String username) {
		return (String) queryForRawObject(getNameSpace() + ".findEmailForUsername", username);
	}
	
	public Integer findUserRoleId(String name) {
		return (Integer) queryForRawObject(getNameSpace() + ".findUserRoleId", name);
	}
}