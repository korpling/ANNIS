/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.objects;

import java.io.Serializable;

import java.net.URI;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is only a wrapper to transport the salt ids with the
 * {@link QueryData} class as extension.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class SaltURIs extends TreeMap<Integer, ArrayList<URI>> implements Serializable
{
}
