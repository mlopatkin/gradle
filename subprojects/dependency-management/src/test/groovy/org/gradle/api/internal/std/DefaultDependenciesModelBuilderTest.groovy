/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.api.internal.std

import com.google.common.collect.Interners
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.StandardOutputListener
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.internal.logging.services.LoggingServiceRegistry
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class DefaultDependenciesModelBuilderTest extends Specification {

    @Subject
    DefaultDependenciesModelBuilder builder = new DefaultDependenciesModelBuilder("libs", Interners.newStrongInterner(), Interners.newStrongInterner())

    @Unroll("#notation is an invalid notation")
    def "reasonable error message if notation is invalid"() {
        when:
        builder.alias("foo", notation)

        then:
        InvalidUserDataException ex = thrown()
        ex.message == 'Invalid dependency notation: it must consist of 3 parts separated by colons, eg: my.group:artifact:1.2'

        where:
        notation << ["", "a", "a:", "a:b", ":b", "a:b:", ":::", "a:b:c:d"]
    }

    @Unroll("#notation is an invalid alias")
    def "reasonable error message if alias is invalid"() {
        when:
        builder.alias(notation, "org:foo:1.0")

        then:
        InvalidUserDataException ex = thrown()
        ex.message == "Invalid alias name '$notation': it must match the following regular expression: [a-z]([a-zA-Z0-9_.\\-])+"

        where:
        notation << ["", "a", "1a", "A", "Aa", "abc\$", "abc&"]
    }

    @Unroll("#notation is an invalid bundle name")
    def "reasonable error message if bundle name is invalid"() {
        when:
        builder.bundle(notation, [])

        then:
        InvalidUserDataException ex = thrown()
        ex.message == "Invalid bundle name '$notation': it must match the following regular expression: [a-z]([a-zA-Z0-9_.\\-])+"

        where:
        notation << ["", "a", "1a", "A", "Aa", "abc\$", "abc&"]
    }

    def "warns if multiple entries use the same alias"() {
        StandardOutputListener listener = Mock()
        def registry = LoggingServiceRegistry.newCommandLineProcessLogging()
        def loggingManager = registry.newInstance(LoggingManagerInternal)
        loggingManager.enableUserStandardOutputListeners()
        loggingManager.addStandardOutputListener(listener)
        loggingManager.start()

        builder.alias("foo", "a:b:1.0")

        when:
        builder.alias("foo", "e:f:1.1")

        then:
        1 * listener.onOutput("Duplicate entry for alias 'foo': dependency {group='a', name='b', version='1.0'} is replaced with dependency {group='e', name='f', version='1.1'}")

        cleanup:
        loggingManager.stop()
    }

    def "warns if multiple entries use the same bundle name"() {
        StandardOutputListener listener = Mock()
        def registry = LoggingServiceRegistry.newCommandLineProcessLogging()
        def loggingManager = registry.newInstance(LoggingManagerInternal)
        loggingManager.enableUserStandardOutputListeners()
        loggingManager.addStandardOutputListener(listener)
        loggingManager.start()

        builder.bundle("foo", ["a", "b"])

        when:
        builder.bundle("foo", ["c", "d", "e"])

        then:
        1 * listener.onOutput("Duplicate entry for bundle 'foo': [a, b] is replaced with [c, d, e]")

        cleanup:
        loggingManager.stop()
    }

    def "fails building the model if a bundle references a non-existing alias"() {
        builder.alias("guava", "com.google.guava:guava:17.0")
        builder.bundle("toto", ["foo"])

        when:
        builder.build()

        then:
        InvalidUserDataException ex = thrown()
        ex.message == "A bundle with name 'toto' declares a dependency on 'foo' which doesn't exist"
    }

    def "model reflects what is declared"() {
        builder.alias("guava", "com.google.guava:guava:17.0")
        builder.alias("groovy", "org.codehaus.groovy", "groovy") {
            it.strictly("3.0.5")
        }
        builder.alias("groovy-json", "org.codehaus.groovy", "groovy-json") {
            it.prefer("3.0.5")
        }
        builder.bundle("groovy", ["groovy", "groovy-json"])

        when:
        def model = builder.build()

        then:
        model.bundleAliases == ["groovy"]
        model.getBundle("groovy") == ["groovy", "groovy-json"]

        model.dependencyAliases == ["groovy", "groovy-json", "guava"]
        model.getDependencyData("guava").version.requiredVersion == '17.0'
        model.getDependencyData("groovy").version.strictVersion == '3.0.5'
        model.getDependencyData("groovy-json").version.strictVersion == ''
        model.getDependencyData("groovy-json").version.preferredVersion == '3.0.5'
    }

    def "strings are interned"() {
        builder.alias("foo", "bar", "baz") {
            it.require "1.0"
        }
        builder.alias("baz", "foo", "bar") {
            it.prefer "1.0"
        }
        when:
        def model = builder.build()

        then:
        def bazKey = model.dependencyAliases.find { it == 'baz' }
        model.getDependencyData("foo").group.is(model.getDependencyData("baz").name)
        model.getDependencyData("foo").name.is(bazKey)
        model.getDependencyData("foo").version.requiredVersion.is(model.getDependencyData("baz").version.preferredVersion)
    }
}