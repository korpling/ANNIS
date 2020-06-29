/*
 * graphANNIS
 * Access the graphANNIS corpora and execute AQL queries with this service. 
 *
 * The version of the OpenAPI document: 0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.corpus_tools.annis.api.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * FrequencyQueryDefinition
 */
@javax.annotation.processing.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-06-29T10:18:56.988+02:00[Europe/Berlin]")
public class FrequencyQueryDefinition {
  public static final String SERIALIZED_NAME_NS = "ns";
  @SerializedName(SERIALIZED_NAME_NS)
  private String ns = "null";

  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_NODE_REF = "node_ref";
  @SerializedName(SERIALIZED_NAME_NODE_REF)
  private String nodeRef;


  public FrequencyQueryDefinition ns(String ns) {
    
    this.ns = ns;
    return this;
  }

   /**
   * The namespace of the annotation from which the attribute value is generated.
   * @return ns
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "const", value = "The namespace of the annotation from which the attribute value is generated.")

  public String getNs() {
    return ns;
  }


  public void setNs(String ns) {
    this.ns = ns;
  }


  public FrequencyQueryDefinition name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * The name of the annotation from which the attribute value is generated.
   * @return name
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "cat", value = "The name of the annotation from which the attribute value is generated.")

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public FrequencyQueryDefinition nodeRef(String nodeRef) {
    
    this.nodeRef = nodeRef;
    return this;
  }

   /**
   * The name of the query node from which the attribute value is generated.
   * @return nodeRef
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "root", value = "The name of the query node from which the attribute value is generated.")

  public String getNodeRef() {
    return nodeRef;
  }


  public void setNodeRef(String nodeRef) {
    this.nodeRef = nodeRef;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FrequencyQueryDefinition frequencyQueryDefinition = (FrequencyQueryDefinition) o;
    return Objects.equals(this.ns, frequencyQueryDefinition.ns) &&
        Objects.equals(this.name, frequencyQueryDefinition.name) &&
        Objects.equals(this.nodeRef, frequencyQueryDefinition.nodeRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ns, name, nodeRef);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FrequencyQueryDefinition {\n");
    sb.append("    ns: ").append(toIndentedString(ns)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    nodeRef: ").append(toIndentedString(nodeRef)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

