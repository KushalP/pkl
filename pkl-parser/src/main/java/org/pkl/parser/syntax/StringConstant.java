/*
 * Copyright © 2025 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.parser.syntax;

import java.util.Objects;
import org.pkl.parser.ParserVisitor;
import org.pkl.parser.Span;
import org.pkl.parser.util.Nullable;

public class StringConstant extends AbstractNode {
  private final String string;

  public StringConstant(String string, Span span) {
    super(span, null);
    this.string = string;
  }

  @Override
  public <T> @Nullable T accept(ParserVisitor<? extends T> visitor) {
    return visitor.visitStringConstant(this);
  }

  public String getString() {
    return string;
  }

  @Override
  public String toString() {
    return "StringConstant{string='" + string + '\'' + ", span=" + span + '}';
  }

  @SuppressWarnings("ConstantValue")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    StringConstant that = (StringConstant) o;
    return Objects.equals(string, that.string);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), string);
  }
}
