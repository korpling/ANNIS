/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LDAPSecurityManager implements AnnisSecurityManager {
	private Properties properties;

	/* (non-Javadoc)
	 * @see annis.security.AnnisSecurityManager#login(java.lang.String, java.lang.String)
	 */
	public AnnisUser login(String userName, String password) throws NamingException {
		String userBaseDN = properties.getProperty("userBaseDN");
		String groupBaseDN = properties.getProperty("groupBaseDN");
		String corpusBaseDN = properties.getProperty("corpusBaseDN");
		
		String ldapId = "cn=" + userName + "," + userBaseDN;
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.SECURITY_PRINCIPAL, ldapId); 
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, this.properties.getProperty("ldapURL"));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");	
		
		DirContext ctx = new InitialDirContext(env);

		SearchControls ctlsSubtree = new SearchControls();
		ctlsSubtree.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		SearchControls ctlsObject = new SearchControls();
		ctlsObject.setSearchScope(SearchControls.OBJECT_SCOPE);

		
		String filterUser = "(objectClass=inetOrgPerson)";

		// Search the subtree for objects by using the filter
		NamingEnumeration<SearchResult> answerUserSearch = ctx.search(ldapId, filterUser, ctlsObject);
		AnnisUser user = null;
		if(answerUserSearch.hasMoreElements()) {
			SearchResult item = answerUserSearch.next();
			Attributes attributes = item.getAttributes();
			
			user = new AnnisUser(attributes.get("cn").get(0).toString(), 
        attributes.get("sn").get(0).toString(), "");
			user.setPassword(password);
			try {
				user.setGivenName(attributes.get("givenName").get(0).toString());
			} catch (NullPointerException e) {
				//ignore
			}
			try {
				user.load(new ByteArrayInputStream(attributes.get("description").get(0).toString().getBytes()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				//No description set
			}
			
			//Load Corpus List from groups
			String filterGroups = "(member=" + ldapId + ")";
			NamingEnumeration<SearchResult> answerGroupSearch = ctx.search(groupBaseDN, filterGroups, ctlsSubtree);
			//Traverse all Groups
			List<Long> corpusIdList = user.getCorpusIdList();
			while(answerGroupSearch.hasMoreElements()) {
				SearchResult group = answerGroupSearch.next();
				//Search for the containing corpus Object
				String groupPath = group.getName() + ((group.isRelative()) ? "," + groupBaseDN : "");
				String filterCorpus = "(member=" + groupPath + ")";
				NamingEnumeration<SearchResult> answerCorpusSearch = ctx.search(corpusBaseDN, filterCorpus, ctlsSubtree);
				while(answerCorpusSearch.hasMoreElements()) {
					SearchResult corpus = answerCorpusSearch.next();
					try {
						corpusIdList.add(Long.parseLong(corpus.getAttributes().get("cn").get(0).toString()));
					} catch (NumberFormatException e) {
						//this corpus has a malformed cn
					}
				}
			}
			return user;
		}
		throw new javax.naming.NamingException("The user Object stored in Directory is not an 'inetOrgPerson'.");
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		Hashtable<String, String> env = new Hashtable<String, String>();	
		
    String userBaseDN = properties.getProperty("userBaseDN");
    String ldapId = "cn=" + user.getUserName() + "," + userBaseDN;
    
		env.put(Context.SECURITY_PRINCIPAL, ldapId); 
		env.put(Context.SECURITY_CREDENTIALS, user.getPassword());
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, this.properties.getProperty("ldapURL"));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");	
		
		DirContext ctx = new InitialDirContext(env);
		user.store(bos, "");
		Attribute mod = new BasicAttribute("description", bos.toString());
		
    ModificationItem[] mods = 
    {
      new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod)
    };
    ctx.modifyAttributes(ldapId, mods);
		
	}
}
