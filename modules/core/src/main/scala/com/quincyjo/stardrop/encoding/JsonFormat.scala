/*
 * Copyright 2023 Quincy Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quincyjo.stardrop.encoding

import io.circe.generic.extras.Configuration

/** Contains default JSON formatting options used by SMAPI mods. Specifically,
  * this includes a configuration which capitalizes all JSON keys.
  */
object JsonFormat {

  /** The default configuration to be used by SMAPI mods.
    */
  final val DefaultConfig: Configuration =
    Configuration.default.copy(transformMemberNames = _.capitalize)
}
