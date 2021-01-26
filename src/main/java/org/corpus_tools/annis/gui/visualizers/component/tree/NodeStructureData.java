/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.component.tree;


class NodeStructureData {
  private int height;
  private final NodeStructureData parent;
  private boolean isContinuous;
  private long leftCorner;
  private long rightCorner;
  private long leftmostImmediate = -1;
  private long rightmostImmediate = -1;
  private long arity = 0;
  private long tokenArity;
  private int step = 0;

  public NodeStructureData(NodeStructureData parent_) {
    parent = parent_;
  }

  public boolean canHaveVerticalOverlap() {
    if (arity == 0) {
      return getHeight() + 1 < parent.getHeight();
    } else {
      return isContinuous;
    }
  }

  public boolean encloses(NodeStructureData other) {
    return leftCorner < other.leftCorner && rightCorner > other.rightCorner;
  }

  public long getArity() {
    return arity;
  }

  public int getHeight() {
    return height + step;
  }

  public long getLeftCorner() {
    return leftCorner;
  }

  public long getLeftmostImmediate() {
    return leftmostImmediate;
  }

  public NodeStructureData getParent() {
    return parent;
  }

  public long getRightCorner() {
    return rightCorner;
  }

  public long getRightmostImmediate() {
    return rightmostImmediate;
  }

  public long getTokenArity() {
    return tokenArity;
  }

  public boolean hasPredecessor(NodeStructureData node) {
    if (node == parent) {
      return true;
    } else if (parent == null) {
      return false;
    } else {
      return parent.hasPredecessor(node);
    }
  }

  /**
   * Tests if the incoming edge of other can conflict (visually overlap) with the incoming edge of
   * this node.
   * 
   * Two vertical dominance edges can only conflict if other's parent node is a predecessor of this
   * node.
   * 
   * @param other some other node.
   * @return true iff the incoming vertical dominance edges can conflict.
   */
  public boolean hasVerticalEdgeConflict(NodeStructureData other) {
    return hasPredecessor(other.parent);
  }

  public void increaseStep() {
    step += 1;
    parent.newChildHeight(getHeight());
  }

  public boolean isContinuous() {
    return isContinuous;
  }

  private void newChildHeight(int newHeight) {
    if (newHeight > this.height) {
      setChildHeight(newHeight);
      if (parent != null) {
        parent.newChildHeight(getHeight());
      }
    }
  }

  public void setArity(long arity) {
    this.arity = arity;
  }

  public void setChildHeight(int height) {
    this.height = height;
  }

  public void setContinuous(boolean isContinuous) {
    this.isContinuous = isContinuous;
  }

  public void setLeftCorner(long leftCorner) {
    this.leftCorner = leftCorner;
  }

  public void setLeftmostImmediate(long leftmostImmediate) {
    this.leftmostImmediate = leftmostImmediate;
  }

  public void setRightCorner(long rightCorner) {
    this.rightCorner = rightCorner;
  }

  public void setRightmostImmediate(long rightmostImmediate) {
    this.rightmostImmediate = rightmostImmediate;
  }

  public void setStep(int newValue) {
    step = newValue;
  }

  public void setTokenArity(long tokenArity) {
    this.tokenArity = tokenArity;
  }
}
