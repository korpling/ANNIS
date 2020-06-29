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
import org.corpus_tools.annis.api.model.AnnoKey;

/**
 * An annotation with a qualified name and a value.
 */
@ApiModel(description = "An annotation with a qualified name and a value.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-06-29T10:18:56.988+02:00[Europe/Berlin]")
public class Annotation {
  public static final String SERIALIZED_NAME_KEY = "key";
  @SerializedName(SERIALIZED_NAME_KEY)
  private AnnoKey key;

  public static final String SERIALIZED_NAME_VAL = "val";
  @SerializedName(SERIALIZED_NAME_VAL)
  private String val;


  public Annotation key(AnnoKey key) {
    
    this.key = key;
    return this;
  }

   /**
   * Get key
   * @return key
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public AnnoKey getKey() {
    return key;
  }


  public void setKey(AnnoKey key) {
    this.key = key;
  }


  public Annotation val(String val) {
    
    this.val = val;
    return this;
  }

   /**
   * Value of the annotation
   * @return val
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "VVFIN", value = "Value of the annotation")

  public String getVal() {
    return val;
  }


  public void setVal(String val) {
    this.val = val;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Annotation annotation = (Annotation) o;
    return Objects.equals(this.key, annotation.key) &&
        Objects.equals(this.val, annotation.val);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, val);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Annotation {\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    val: ").append(toIndentedString(val)).append("\n");
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
