/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
class App(val name: String,
          @JsonIgnore
          @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true, fetch = FetchType.LAZY)
          @OnDelete(action = OnDeleteAction.CASCADE)
          var reporter: User) {

    var configuration: Configuration = Configuration()

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id = 0

    override fun equals(other: Any?) = when {
        this === other -> true
        other == null || javaClass != other.javaClass -> false
        else -> id == (other as App).id
    }

    override fun hashCode() = id

    @Embeddable
    class Configuration(@Access(AccessType.FIELD) val minScore: Int = 95)
}