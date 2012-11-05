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
package co.jirm.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import co.jirm.orm.JirmFactory;
import co.jirm.orm.dao.JirmDao;
import co.jirm.orm.dao.JirmOpportunisticLockException;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/test-applicationContext.xml")
public class JirmDaoIntegrationTest {


	@Autowired
	private JirmFactory jirmFactory;
	private JirmDao<TestBean> dao;
	
	@Before
	public void setUp() throws Exception {
		dao = jirmFactory.daoFor(TestBean.class);
	}

	@Test
	public void testAll() throws Exception {
		TestBean t = new TestBean(randomId(), 2L, Calendar.getInstance());
		dao.insert(t);
		List<TestBean> list = //dao.queryForListByFilter("stringProp", "hello");
				dao.select().where()
					.property("stringProp").eq("hello")
					.query()
					.forList();
		long count = dao.count().query().forLong();
		assertTrue(count > 0);
		
		TestBean newT = new TestBean(t.getStringProp(), 50, Calendar.getInstance());
		dao.update(newT);
		TestBean reloaded = dao.reload(newT);
		assertTrue(reloaded != newT);
		assertTrue(reloaded.getStringProp().equals(newT.getStringProp()));
		assertNotNull(list);
		
	}
	
	@Test
	public void testAdding() throws Exception {
		String id = randomId();
		TestBean t = new TestBean(id, 2L, Calendar.getInstance());
		dao.insert(t);
		int i = dao.update().plus("longProp", 100).where().property("stringProp", id).execute();
		assertTrue(i > 0);
		
	}
	
	@Test
	public void testManyToOne() throws Exception {
		TestBean t = new TestBean(randomId(), 2L, Calendar.getInstance());
		dao.insert(t);
		ParentBean pb = new ParentBean(randomId(), t);
		jirmFactory.daoFor(ParentBean.class).insert(pb);
		jirmFactory.daoFor(GrandParentBean.class).insert(new GrandParentBean(randomId(), pb));
		
		pb = jirmFactory.daoFor(ParentBean.class).reload(pb);
		LazyGrandParentBean lazy = jirmFactory.daoFor(LazyGrandParentBean.class)
				.select()
					.where("{{parent.id}} = ?").with(pb.getId())
					.query()
					.forObject();
		assertTrue( ! lazy.getParent().getTest().isPresent());
		
		assertNotNull(pb.getTest());
		
	}
	
	@Test
	public void testBatch() throws Exception {
		List<TestBean> batchBeans = Lists.newArrayListWithExpectedSize(300);
		
		for (int i = 0; i < 300; i++) {
			TestBean t = new TestBean(randomId(), 5000L, Calendar.getInstance());
			batchBeans.add(t);
		}
		
		dao.insert(batchBeans.iterator(), 50);
		dao.delete()
			.where()
			.property("longProp", 5000L)
			.execute();
	}
	
	@Ignore
	@Test
	public void testNonBatch() throws Exception {
		List<TestBean> batchBeans = Lists.newArrayListWithExpectedSize(300);
		
		for (int i = 0; i < 300; i++) {
			TestBean t = new TestBean(randomId(), 5000L, Calendar.getInstance());
			batchBeans.add(t);
		}
		
		for (TestBean t: batchBeans) {
			dao.insert(t);
		}
	}
	
	@Test
	public void testVersion() throws Exception {
		JirmDao<LockBean> dao = jirmFactory.daoFor(LockBean.class);
		LockBean lockBean = new LockBean(randomId(), 100L, Calendar.getInstance(), 0);
		dao.insert(lockBean);
		lockBean = new LockBean(lockBean.getId(), 300L, Calendar.getInstance(), lockBean.getVersion());
		dao.update(lockBean);
	}
	
	@Test(expected=JirmOpportunisticLockException.class)
	public void testVersionFail() throws Exception {
		JirmDao<LockBean> dao = jirmFactory.daoFor(LockBean.class);
		LockBean lockBean = new LockBean(randomId(), 100L, Calendar.getInstance(), 0);
		dao.insert(lockBean);
		lockBean = new LockBean(lockBean.getId(), 300L, Calendar.getInstance(), lockBean.getVersion());
		dao.update(lockBean);
		//This will fail because we have to reload.
		dao.update(lockBean);
	}
	
	public static String randomId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
}