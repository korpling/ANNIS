/*
 * Copyright 2013 SFB 632.
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
package annis.service.internal;

import annis.dao.QueryDao;
import annis.model.Annotation;
import annis.service.objects.AnnisBinaryMetaData;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@Path("annis/meta")
public class MetadataService {

    private Logger log = LoggerFactory.getLogger(MetadataService.class);

    @Context
    Configuration config;

    @GET
    @Path("binary/{top}/{doc}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<AnnisBinaryMetaData> binaryMeta(@PathParam("top") String toplevelCorpusName,
            @PathParam("doc") String doc) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + toplevelCorpusName);

        return getQueryDao().getBinaryMeta(toplevelCorpusName, doc);
    }

    @GET
    @Path("docnames/{toplevel}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getDocNames(@PathParam("toplevel") String topLevelCorpus) throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevelCorpus);

        return getQueryDao().listDocuments(topLevelCorpus);
    }

    @GET
    @Path("corpus/{toplevel}/closure")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getMetadata(@PathParam("toplevel") String topLevelCorpus) throws GraphANNISException {
        return getMetadata(topLevelCorpus, true);
    }

    public List<Annotation> getMetadata(@PathParam("toplevel") String topLevelCorpus,
            @DefaultValue(value = "false") boolean closure) throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevelCorpus);

        return getQueryDao().listDocumentsAnnotations(topLevelCorpus, closure);
    }

    @GET
    @Path("doc/{toplevel}/{doc}/path")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getMetadataDoc(@PathParam("toplevel") String toplevelCorpus, @PathParam("doc") String doc)
            throws GraphANNISException {
        return getMetadataDoc(toplevelCorpus, doc, false);
    }

    public List<Annotation> getMetadataDoc(String topLevelCorpus, String docname, boolean path)
            throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevelCorpus);

        return getQueryDao().listCorpusAnnotations(topLevelCorpus, docname, path);
    }

    @GET
    @Path("doc/{toplevel}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getMetaDataDoc(@PathParam("toplevel") String topLevel) throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevel);

        return getQueryDao().listDocumentsAnnotations(topLevel, false);
    }

    @GET
    @Path("doc/{toplevel}/{doc}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getMetaDataDoc(@PathParam("toplevel") String topLevelCorpus, @PathParam("doc") String doc)
            throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevelCorpus);

        return getQueryDao().listCorpusAnnotations(topLevelCorpus, doc, true);
    }

    @GET
    @Path("corpus/{toplevel}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Annotation> getMetadataTopLevel(@PathParam("toplevel") String topLevelCorpus)
            throws GraphANNISException {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("meta:" + topLevelCorpus);

        return getQueryDao().listCorpusAnnotations(topLevelCorpus);
    }

    private QueryDao getQueryDao() {
        Object prop = config.getProperty("queryDao");
        if (prop instanceof QueryDao) {
            return (QueryDao) prop;
        } else {
            return null;
        }
    }

    /**
     * Log the successful initialization of this bean.
     *
     * <p>
     * XXX: This should be a private method annotated with <tt>@PostConstruct</tt>,
     * but that doesn't seem to work. As a work-around, the method is called by
     * Spring as an init-method.
     */
    public void init() { // log a message after successful startup
        log.info("ANNIS MetadataService loaded.");
    }

}
