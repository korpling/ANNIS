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
 * Defines a subgraph of an annotation graph using node IDs and a context.
 */
@ApiModel(description = "Defines a subgraph of an annotation graph using node IDs and a context.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2020-06-28T18:59:24.261+02:00[Europe/Berlin]")
public class SubgraphWithContext {
  public static final String SERIALIZED_NAME_NODE_IDS = "node_ids";
  @SerializedName(SERIALIZED_NAME_NODE_IDS)
  private List<String> nodeIds = null;

  public static final String SERIALIZED_NAME_SEGMENTATION = "segmentation";
  @SerializedName(SERIALIZED_NAME_SEGMENTATION)
  private String segmentation = "null";

  public static final String SERIALIZED_NAME_LEFT = "left";
  @SerializedName(SERIALIZED_NAME_LEFT)
  private Integer left = 0;

  public static final String SERIALIZED_NAME_RIGHT = "right";
  @SerializedName(SERIALIZED_NAME_RIGHT)
  private Integer right = 0;


  public SubgraphWithContext nodeIds(List<String> nodeIds) {
    
    this.nodeIds = nodeIds;
    return this;
  }

  public SubgraphWithContext addNodeIdsItem(String nodeIdsItem) {
    if (this.nodeIds == null) {
      this.nodeIds = new ArrayList<String>();
    }
    this.nodeIds.add(nodeIdsItem);
    return this;
  }

   /**
   * A list of node IDs that should be part of the subgraph.
   * @return nodeIds
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "[\"GUM/GUM_whow_skittles#tok_925\",\"GUM/GUM_whow_skittles#tok_926\"]", value = "A list of node IDs that should be part of the subgraph.")

  public List<String> getNodeIds() {
    return nodeIds;
  }


  public void setNodeIds(List<String> nodeIds) {
    this.nodeIds = nodeIds;
  }


  public SubgraphWithContext segmentation(String segmentation) {
    
    this.segmentation = segmentation;
    return this;
  }

   /**
   * Segmentation to use for defining the context, Set to null or omit it if tokens should be used.
   * @return segmentation
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "Segmentation to use for defining the context, Set to null or omit it if tokens should be used.")

  public String getSegmentation() {
    return segmentation;
  }


  public void setSegmentation(String segmentation) {
    this.segmentation = segmentation;
  }


  public SubgraphWithContext left(Integer left) {
    
    this.left = left;
    return this;
  }

   /**
   * Left context size.
   * @return left
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "5", value = "Left context size.")

  public Integer getLeft() {
    return left;
  }


  public void setLeft(Integer left) {
    this.left = left;
  }


  public SubgraphWithContext right(Integer right) {
    
    this.right = right;
    return this;
  }

   /**
   * Right context size.
   * @return right
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(example = "5", value = "Right context size.")

  public Integer getRight() {
    return right;
  }


  public void setRight(Integer right) {
    this.right = right;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubgraphWithContext subgraphWithContext = (SubgraphWithContext) o;
    return Objects.equals(this.nodeIds, subgraphWithContext.nodeIds) &&
        Objects.equals(this.segmentation, subgraphWithContext.segmentation) &&
        Objects.equals(this.left, subgraphWithContext.left) &&
        Objects.equals(this.right, subgraphWithContext.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeIds, segmentation, left, right);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubgraphWithContext {\n");
    sb.append("    nodeIds: ").append(toIndentedString(nodeIds)).append("\n");
    sb.append("    segmentation: ").append(toIndentedString(segmentation)).append("\n");
    sb.append("    left: ").append(toIndentedString(left)).append("\n");
    sb.append("    right: ").append(toIndentedString(right)).append("\n");
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

