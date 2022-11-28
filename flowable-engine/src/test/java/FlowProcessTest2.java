import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class FlowProcessTest2 {
    ProcessEngine processEngine =null;
    @Before
    public void testProcessEngine(){
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://192.168.56.104:3306/flowable-showcase1?characterEncoding=UTF-8")
                .setJdbcUsername("root")
                .setJdbcPassword("root")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
//                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngine = cfg.buildProcessEngine();
        System.out.println("processEngine = " + processEngine);
    }

    /**
     * 部署一个流程
     */
    @Test
    public void testDeploy(){
        RepositoryService repositoryService=processEngine.getRepositoryService();
        Deployment deployment=repositoryService.createDeployment().addClasspathResource("Common.bpmn20.xml").name("通用流程").deploy();
        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    /**
     * 压缩文件部署
     */
    @Test
    public void testZipDeploy(){
        RepositoryService repositoryService=processEngine.getRepositoryService();

        ZipInputStream zipInputStream=new ZipInputStream(this.getClass().getResourceAsStream("holiday.bar"));
        Deployment deployment=repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    /**
     * 获取流程定义信息
     */
    @Test
    public void testGetDeployProcessDefine(){

        RepositoryService repositoryService=processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId("1").singleResult();
        System.out.println("processDefinition.getName() = " + processDefinition.getName());
        System.out.println("processDefinition.getId() = " + processDefinition.getId());
    }
    /**
     * 启动流程
     */
    @Test
    public void testStartProcess(){
        RuntimeService runtimeService=processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("description", "启动流程");
        ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("CommonProcess",variables);
        System.out.println("processInstance.getId() = " + processInstance.getId());
        System.out.println("processInstance.getProcessVariables() = " + processInstance.getProcessVariables());
        //processInstance.getId() = 2501
        //processInstance.getProcessVariables() = {description=启动流程}

    }

    /**
     * 获取所有进行中的流程示例
     */
    @Test
    public void testAllProcessInstance(){
        RuntimeService runtimeService=processEngine.getRuntimeService();
        List<ActivityInstance> activityInstances= runtimeService.createActivityInstanceQuery().activityInstanceId("2501").
        list();
        activityInstances.forEach(instance->{
            System.out.println("instance.getId() = " + instance.getId());
            System.out.println("instance.getActivityName() = " + instance.getActivityName());
        });
    }
    /**
     * 申请人获取task
     */
    @Test
    public void testTask(){
        TaskService taskService =processEngine.getTaskService();
       List<Task> taskList= taskService.createTaskQuery().list();
       taskList.forEach(task -> {

           System.out.println("\ntask = " + task);
           Map<String, Object> variables = new HashMap<String, Object>();
           variables.put("employee", "张");
           variables.put("nrOfHolidays", "3");
           variables.put("description", "请假三天");

           //完成操作
           taskService.complete(task.getId());
       });

    }
        @Test
        public void testMangerTask(){
            TaskService taskService =processEngine.getTaskService();
            List<Task> taskList= taskService.createTaskQuery().list();
            taskList.forEach(task -> {
                System.out.println("\ntask = " + task);
                System.out.println("task.getProcessInstanceId() = " + task.getProcessInstanceId());
            });
        }
    @Test
    public void testHistoryTask(){
//        HistoryService historyService =processEngine.getHistoryService();
//         historyService.createHistoricActivityInstanceQuery().processInstanceId("2501").finished().list()
//        historicDetails.forEach(historicDetail -> {
//            System.out.println("\n historicDetails = " + historicDetail);
//
//        });
    }


}
