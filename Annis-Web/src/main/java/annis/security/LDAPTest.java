/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LDAPTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String userName = "khuetter";
		String password = "hirg";
		
		userName = "cn=" + userName + ",ou=users,dc=localdomain";
		
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.SECURITY_PRINCIPAL, userName); 
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://192.168.56.129:389");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		
		AnnisUser user = new AnnisUser(userName, "khuetter", "Huetter");
		user.put("key1", "value1");
		user.put("key2", "value2");
		user.put("key3", "value3");
		user.put("key4", "value4");
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			
			DirContext ctx = new InitialDirContext(env);
			user.store(bos, "");
			
			Attribute mod = new BasicAttribute("description", bos.toString());
			
            ModificationItem[] mods = {new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod)};
			ctx.modifyAttributes(userName, mods);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done");
	}

}
