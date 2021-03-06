/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Lee Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.javaconductor.gserv.utils

import io.github.javaconductor.gserv.ResourceAction
import io.github.javaconductor.gserv.ActionPathElement
import io.github.javaconductor.gserv.Utils

/**
 * A LinkBuilder is used to create URLs to resources
 *
 */
class LinkBuilder {
    def prefix
    def actions = [:]

    def LinkBuilder(prefix = null) {
        this.prefix = prefix
    }

    def LinkBuilder(prefix, actions) {
        this(prefix)
        this.actions = actions
    }

    /**
     * Creates a new LinkBuilder combining actions and retaining the prefix from 'this'
     *
     * @param lb LinkBuilder to merge
     * @return New LinkBuilder
     */
    def plus(LinkBuilder lb) {
        lb ? new LinkBuilder(prefix: prefix, actions: actions + lb.actions)
                : this
    }

    /**
     * Add route by name
     *
     * @param name The name LinkBuilder will associate with the Route.
     * @param action
     * @return
     */
    @Deprecated
    def add(String name, ResourceAction action) {
        addLink(name, action)
    }
    /**
     * Add action by name
     *
     * @param name The name LinkBuilder will associate with the Action.
     * @param route
     * @return
     */
    def addLink(String name, ResourceAction action) {
        actions[name] = action
    }
    /**
     *
     * @param name Route name
     * @param data Data used to interpolate the resulting URL
     * @return URI to a named route
     */
    def link(name, data) {
        // find the action in actions
        ResourceAction action = actions[name]
        if (!action) {
            throw new IllegalArgumentException("No such action '$name'. Be sure to add a value for 'actionName' in your action definition.")
        }

        // loop thru the path and on non-var we append it and on var we append the value from data
        def link = ""
        for (int i = 0; i != action.pathSize(); ++i) {
            ActionPathElement element = action.path(i)
            if (element._isVar) {
                def d = data[element.variableName()]
                if (!d) {
                    throw new IllegalArgumentException("${element.variableName()} not found in data. data must contain values for all action variables. ")
                }
                link += "/$d"
            } else {
                link += "/${element.text()}"
            }
        }

        action.queryPattern().queryMap().eachWithIndex { it, i ->
            def k = it.key, v = it.value

            if (Utils.isMatchingPattern(v)) {
                def d = data[k]
                if (!d) {
                    throw new IllegalArgumentException("${k} not found in data. data must contain values for all action variables. ")
                }
                link += ((i == 0 ? '?' : '&') + "$k=$d")

            } else if (Utils.isValuePattern(v)) {
                link += ((i == 0 ? '?' : '&') + "$k=$v")
            }
        }
        link
    }
}
