/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.core.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jflute
 */
public class UserMessages implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    /** The property key for global property (non-specific property) for protocol with HTML, JavaScipt. */
    public static final String GLOBAL_PROPERTY_KEY = "_global";

    protected static final Comparator<UserMessageItem> actionItemComparator = (item1, item2) -> {
        return item1.getItemOrder() - item2.getItemOrder();
    };

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, UserMessageItem> messageMap = new LinkedHashMap<String, UserMessageItem>();
    protected boolean accessed;
    protected int itemCount;
    protected Map<String, Object> successAttributeMap; // lazy loaded

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public UserMessages() {
    }

    // ===================================================================================
    //                                                                         Add Message
    //                                                                         ===========
    public void add(String property, UserMessage message) {
        assertArgumentNotNull("property", property);
        assertArgumentNotNull("message", message);
        final UserMessageItem item = messageMap.get(property);
        final List<UserMessage> messageList;
        if (item == null) {
            ++itemCount;
            messageList = new ArrayList<UserMessage>();
            messageMap.put(property, newUserMessageItem(messageList, itemCount, property));
        } else {
            messageList = item.getMessageList();
        }
        messageList.add(message);
    }

    protected UserMessageItem newUserMessageItem(List<UserMessage> messageList, int itemCount, String property) {
        return new UserMessageItem(messageList, itemCount, property);
    }

    public void add(UserMessages messages) {
        assertArgumentNotNull("messages", messages);
        for (String property : messages.toPropertySet()) {
            for (Iterator<UserMessage> ite = messages.accessByIteratorOf(property); ite.hasNext();) {
                add(property, ite.next());
            }
        }
    }

    // ===================================================================================
    //                                                                      Access Message
    //                                                                      ==============
    public Iterator<UserMessage> accessByFlatIterator() {
        accessed = true;
        if (messageMap.isEmpty()) {
            return Collections.emptyIterator();
        }
        return doAccessByFlatIterator();
    }

    protected Iterator<UserMessage> doAccessByFlatIterator() {
        final List<UserMessage> msgList = new ArrayList<UserMessage>();
        final List<UserMessageItem> itemList = new ArrayList<UserMessageItem>(messageMap.size());
        for (Iterator<UserMessageItem> ite = messageMap.values().iterator(); ite.hasNext();) {
            itemList.add(ite.next());
        }
        Collections.sort(itemList, actionItemComparator);
        for (Iterator<UserMessageItem> ite = itemList.iterator(); ite.hasNext();) {
            for (Iterator<UserMessage> messages = ite.next().getMessageList().iterator(); messages.hasNext();) {
                msgList.add(messages.next());
            }
        }
        return msgList.iterator();
    }

    public Iterator<UserMessage> accessByIteratorOf(String property) {
        assertArgumentNotNull("property", property);
        accessed = true;
        return doAccessByIteratorOf(property);
    }

    public Iterator<UserMessage> nonAccessByIteratorOf(String property) { // e.g. for logging
        assertArgumentNotNull("property", property);
        return doAccessByIteratorOf(property);
    }

    protected Iterator<UserMessage> doAccessByIteratorOf(String property) {
        final UserMessageItem item = messageMap.get(property);
        return item != null ? item.getMessageList().iterator() : Collections.emptyIterator();
    }

    // ===================================================================================
    //                                                                   Property Handling
    //                                                                   =================
    public boolean hasMessageOf(String property) {
        final UserMessageItem item = messageMap.get(property);
        return item != null && !item.getMessageList().isEmpty();
    }

    public boolean hasMessageOf(String property, String key) {
        final UserMessageItem item = messageMap.get(property);
        return item != null && item.getMessageList().stream().anyMatch(message -> message.getMessageKey().equals(key));
    }

    public Set<String> toPropertySet() {
        return !messageMap.isEmpty() ? Collections.unmodifiableSet(messageMap.keySet()) : Collections.emptySet();
    }

    // ===================================================================================
    //                                                                   Various Operation
    //                                                                   =================
    public void clear() {
        messageMap.clear();
    }

    public boolean isEmpty() {
        return messageMap.isEmpty();
    }

    public boolean isAccessed() {
        return accessed;
    }

    public int size() {
        int total = 0;
        for (Iterator<UserMessageItem> ite = messageMap.values().iterator(); ite.hasNext();) {
            total += ite.next().getMessageList().size();
        }
        return total;
    }

    public int size(String property) {
        assertArgumentNotNull("property", property);
        final UserMessageItem item = messageMap.get(property);
        return item != null ? item.getMessageList().size() : 0;
    }

    // ===================================================================================
    //                                                                        Success Data
    //                                                                        ============
    /**
     * Save validation success attribute, derived in validation process, e.g. selected data, <br>
     * to avoid duplicate database access between validation and main logic.
     * <pre>
     * String <span style="color: #553000">keyOfProduct</span> = Product.<span style="color: #70226C">class</span>.getName();
     * ValidationSuccess <span style="color: #553000">success</span> = validate(<span style="color: #553000">form</span>, <span style="color: #553000">messages</span> <span style="font-size: 120%">-</span>&gt;</span> {
     *     ...
     *     selectProduct().ifPresent(<span style="color: #553000">product</span> <span style="font-size: 120%">-</span>&gt;</span> {}, () <span style="font-size: 120%">-</span>&gt;</span> {
     *         <span style="color: #553000">messages</span>.<span style="color: #CC4747">saveSuccessData</span>(<span style="color: #553000">keyOfProduct</span>, <span style="color: #553000">product</span>);
     *     }).orElse(() <span style="font-size: 120%">-</span>&gt;</span> {}, () <span style="font-size: 120%">-</span>&gt;</span> {
     *         <span style="color: #553000">messages</span>.addConstraint...();
     *     });
     * }, () <span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #70226C">return</span> asHtml(path_Product_ProductListJsp);
     * });
     * <span style="color: #553000">success</span>.<span style="color: #994747">getAttribute</span>(keyOfProduct).alwaysPresent(product <span style="font-size: 120%">-</span>&gt;</span> {
     *     ...
     * });
     * </pre>
     * @param key The string key of the success attribute. (NotNull)
     * @param value The value of success attribute. (NotNull)
     */
    public void saveSuccessAttribute(String key, Object value) {
        assertArgumentNotNull("key", key);
        assertArgumentNotNull("value", value);
        if (successAttributeMap == null) {
            successAttributeMap = new LinkedHashMap<String, Object>(4);
        }
        successAttributeMap.put(key, value);
    }

    /**
     * @return The read-only map of success attribute. (NotNull)
     */
    public Map<String, Object> getSuccessAttributeMap() {
        return successAttributeMap != null ? Collections.unmodifiableMap(successAttributeMap) : Collections.emptyMap();
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The variableName should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return messageMap.toString();
    }

    // ===================================================================================
    //                                                                        Message Item
    //                                                                        ============
    protected static class UserMessageItem implements Serializable {

        private static final long serialVersionUID = 1L;

        protected final List<UserMessage> messageList;
        protected final int itemOrder;
        protected final String property;

        public UserMessageItem(List<UserMessage> messageList, int itemOrder, String property) {
            this.messageList = messageList;
            this.itemOrder = itemOrder;
            this.property = property;
        }

        @Override
        public String toString() {
            return messageList.toString();
        }

        public List<UserMessage> getMessageList() {
            return messageList;
        }

        public int getItemOrder() {
            return itemOrder;
        }

        public String getProperty() {
            return property;
        }
    }
}
