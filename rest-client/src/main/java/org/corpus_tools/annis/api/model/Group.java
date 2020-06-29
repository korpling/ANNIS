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
import java.util.ArrayList;
import java.util.List;

/**
 * Group
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-06-29T10:18:56.988+02:00[Europe/Berlin]")
public class Group {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_CORPORA = "corpora";
  @SerializedName(SERIALIZED_NAME_CORPORA)
  private List<String> corpora = null;


  public Group name(String name) {
    
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "academic", value = "")

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }


  public Group corpora(List<String> corpora) {
    
    this.corpora = corpora;
    return this;
  }

  public Group addCorporaItem(String corporaItem) {
    if (this.corpora == null) {
      this.corpora = new ArrayList<String>();
    }
    this.corpora.add(corporaItem);
    return this;
  }

   /**
   * List of corpus names/identifiers.
   * @return corpora
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "[\"GUM\",\"pcc2.1\"]", value = "List of corpus names/identifiers.")

  public List<String> getCorpora() {
    return corpora;
  }


  public void setCorpora(List<String> corpora) {
    this.corpora = corpora;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Group group = (Group) o;
    return Objects.equals(this.name, group.name) &&
        Objects.equals(this.corpora, group.corpora);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, corpora);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Group {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    corpora: ").append(toIndentedString(corpora)).append("\n");
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
