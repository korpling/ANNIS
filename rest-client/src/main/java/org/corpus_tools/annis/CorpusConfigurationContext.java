/*
 * graphANNIS
 * Access the graphANNIS corpora and execute AQL queries with this service. 
 *
 * OpenAPI spec version: 0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.corpus_tools.annis;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Configuration for configuring context in subgraph queries.
 */
@Schema(description = "Configuration for configuring context in subgraph queries.")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-06-24T14:51:23.486442+02:00[Europe/Berlin]")
public class CorpusConfigurationContext {
  @SerializedName("default")
  private Integer _default = null;

  @SerializedName("sizes")
  private List<Integer> sizes = null;

  @SerializedName("max")
  private Integer max = null;

  @SerializedName("segmentation")
  private String segmentation = null;

  public CorpusConfigurationContext _default(Integer _default) {
    this._default = _default;
    return this;
  }

   /**
   * The default context size.
   * @return _default
  **/
  @Schema(description = "The default context size.")
  public Integer getDefault() {
    return _default;
  }

  public void setDefault(Integer _default) {
    this._default = _default;
  }

  public CorpusConfigurationContext sizes(List<Integer> sizes) {
    this.sizes = sizes;
    return this;
  }

  public CorpusConfigurationContext addSizesItem(Integer sizesItem) {
    if (this.sizes == null) {
      this.sizes = new ArrayList<Integer>();
    }
    this.sizes.add(sizesItem);
    return this;
  }

   /**
   * Available context sizes to choose from.
   * @return sizes
  **/
  @Schema(example = "[1,2,5,10]", description = "Available context sizes to choose from.")
  public List<Integer> getSizes() {
    return sizes;
  }

  public void setSizes(List<Integer> sizes) {
    this.sizes = sizes;
  }

  public CorpusConfigurationContext max(Integer max) {
    this.max = max;
    return this;
  }

   /**
   * If set, a maximum context size which should be enforced by the query system.
   * @return max
  **/
  @Schema(example = "25", description = "If set, a maximum context size which should be enforced by the query system.")
  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }

  public CorpusConfigurationContext segmentation(String segmentation) {
    this.segmentation = segmentation;
    return this;
  }

   /**
   * Default segmentation to use for defining the context, Set to null or omit it if tokens should be used.
   * @return segmentation
  **/
  @Schema(example = "dipl", description = "Default segmentation to use for defining the context, Set to null or omit it if tokens should be used.")
  public String getSegmentation() {
    return segmentation;
  }

  public void setSegmentation(String segmentation) {
    this.segmentation = segmentation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CorpusConfigurationContext corpusConfigurationContext = (CorpusConfigurationContext) o;
    return Objects.equals(this._default, corpusConfigurationContext._default) &&
        Objects.equals(this.sizes, corpusConfigurationContext.sizes) &&
        Objects.equals(this.max, corpusConfigurationContext.max) &&
        Objects.equals(this.segmentation, corpusConfigurationContext.segmentation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_default, sizes, max, segmentation);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CorpusConfigurationContext {\n");
    
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
    sb.append("    sizes: ").append(toIndentedString(sizes)).append("\n");
    sb.append("    max: ").append(toIndentedString(max)).append("\n");
    sb.append("    segmentation: ").append(toIndentedString(segmentation)).append("\n");
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