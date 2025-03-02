/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.TaskGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * project service test
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class TaskGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupServiceTest.class);

    @InjectMocks
    private TaskGroupServiceImpl taskGroupService;

    @Mock
    private TaskGroupQueueService taskGroupQueueService;

    @Mock
    private ProcessService processService;

    @Mock
    private TaskGroupMapper taskGroupMapper;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Mock
    private UserMapper userMapper;

    private String taskGroupName = "TaskGroupServiceTest";

    private String taskGroupDesc = "this is a task group";

    private String userName = "taskGroupServiceTest";

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    private TaskGroup getTaskGroup() {
        TaskGroup taskGroup = new TaskGroup(taskGroupName,0, taskGroupDesc,
            100, 1,1);
        return taskGroup;
    }

    private List<TaskGroup> getList() {
        List<TaskGroup> list = new ArrayList<>();
        list.add(getTaskGroup());
        return list;
    }

    @Test
    public void testCreate() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.insert(taskGroup)).thenReturn(1);
        Mockito.when(taskGroupMapper.queryByName(loginUser.getId(), taskGroupName)).thenReturn(null);
        Map<String, Object> result = taskGroupService.createTaskGroup(loginUser,0, taskGroupName, taskGroupDesc, 100);
        Assert.assertNotNull(result);

    }

    @Test
    public void testQueryById() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result = taskGroupService.queryTaskGroupById(loginUser, 1);
        Assert.assertNotNull(result.get(Constants.DATA_LIST));
    }

    @Test
    public void testQueryProjectListPaging() {

        IPage<TaskGroup> page = new Page<>(1, 10);
        page.setRecords(getList());
        User loginUser = getLoginUser();
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(10),
            Mockito.eq(null), Mockito.eq(0))).thenReturn(page);

        // query all
        Map<String, Object> result = taskGroupService.queryAllTaskGroup(loginUser, null, null,1,10);
        PageInfo<TaskGroup> pageInfo = (PageInfo<TaskGroup>) result.get(Constants.DATA_LIST);
        Assert.assertNotNull(pageInfo.getTotalList());
    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        taskGroup.setStatus(Flag.YES.getCode());
        // Task group status error
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result = taskGroupService.updateTaskGroup(loginUser, 1, "newName", "desc", 100);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        taskGroup.setStatus(0);
    }

    @Test
    public void testCloseAndStart() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);

        //close failed
        Map<String, Object> result1 = taskGroupService.closeTaskGroup(loginUser, 1);
        Assert.assertEquals(Status.SUCCESS, result1.get(Constants.STATUS));
    }

    @Test
    public void testWakeTaskFroceManually() {

        TreeMap<Integer, Integer> tm = new TreeMap<>();
        tm.put(1, 1);
        Map<String, Object> map1 = taskGroupService.forceStartTask(getLoginUser(), 1);
        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));

    }
}
