/**
 * Copyright (C) 2012 the original author or authors.
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
package co.jirm.orm.dao;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


@Table(name="grand_parent_bean")
public class LazyGrandParentBean {

	@Id
	private final String id;
	
	@ManyToOne(targetEntity=LazyParentBean.class, fetch=FetchType.LAZY)
	private final LazyParentBean parent;
	
	@JsonCreator
	public LazyGrandParentBean(
			@JsonProperty("id") String id, 
			@JsonProperty("parent") LazyParentBean parent) {
		super();
		this.id = id;
		this.parent = parent;
	}
	
	
	public String getId() {
		return id;
	}
	
	public LazyParentBean getParent() {
		return parent;
	}
	
}
