import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

public class FlowProcessTest3 {
    ProcessEngine processEngine = null;

    @Before
    public void testProcessEngine() {
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
    public void testDeploy() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.
                createDeployment().
                addClasspathResource("Common.bpmn20.xml").name("通用流程")
                .deploy();
        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    /**
     * 压缩文件部署
     */
    @Test
    public void testZipDeploy() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        ZipInputStream zipInputStream = new ZipInputStream(this.getClass().getResourceAsStream("holiday.bar"));
        Deployment deployment = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    /**
     * 获取流程定义信息
     */
    @Test
    public void testGetDeployProcessDefine() {

        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
//        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey("CommonProcess").list();
        processDefinitions.forEach(processDefinition -> {
            System.out.println("\nprocessDefinition.getId() = " + processDefinition.getId());
            System.out.println("processDefinition.getKey() = " + processDefinition.getKey());
            System.out.println("processDefinition.getName() = " + processDefinition.getName());
            System.out.println("processDefinition.getDiagramResourceName() = " + processDefinition.getDiagramResourceName());
            System.out.println("###############################");
        });

    }

    /**
     * 启动流程
     */
    @Test
    public void testStartProcess() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<String, Object>();
        //指定 assignee 分配到任务的人
        variables.put("createUser", "张三");
        variables.put("description", "启动流程");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CommonProcess", variables);
        System.out.println("processInstance.getId() = " + processInstance.getId());
        System.out.println("processInstance.getProcessVariables() = " + processInstance.getProcessVariables());

    }

    /**
     * 获取所有进行中的流程
     */
    @Test
    public void testAllProcessInstance() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<ActivityInstance> activityInstances = runtimeService.createActivityInstanceQuery().list();
//        List<ActivityInstance> activityInstances= runtimeService.createActivityInstanceQuery().processInstanceId("2501").list();
        activityInstances.forEach(instance -> {
            System.out.println("\ninstance.getId() = " + instance.getId());
            System.out.println("instance.getActivityName() = " + instance.getActivityName());

        });
    }

    /**
     * 获取启动的流程示例
     */
    @Test
    public void testHistoryInstance() {
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("CommonProcess").list();
        historicProcessInstances.forEach(hi -> {
            System.out.println("\nhi.getId() = " + hi.getId() + "   hi.getName() = " + hi.getName() + " hi.getProcessVariables() = " + hi.getProcessVariables());
        });
    }

    /**
     * 申请人获取task
     */
    @Test
    public void testTask() {
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery().taskAssignee("张三").list();
        taskList.forEach(task -> {

            System.out.println("\ntask = " + task);
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("description", "填写完成");

//           完成操作
            taskService.complete(task.getId(), variables);
        });

    }

    @Test
    public void testMangerTask() {
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery().list();
        taskList.forEach(task -> {
            System.out.println("\ntask = " + task);
            System.out.println("task.getProcessInstanceId() = " + task.getProcessInstanceId());
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
        });
    }

    @Test
    public void testHistoryTask() {
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId("2501")
                .finished()
                .list();
        historicTaskInstances.forEach(historicDetail -> {
            System.out.println("historicDetail.getId() = " + historicDetail.getId());
            System.out.println("historicDetail.getName() = " + historicDetail.getName());
            System.out.println("historicDetail.getProcessVariables() = " + historicDetail.getProcessVariables());
            System.out.println("############################");

        });
    }


    /**
     * 获取制定流程当前节点图片
     */
    @Test
    public void testProcessImageByInstance() {
        String processInstanceId = "2501";
        String processDefinitionId = "CommonProcess:1:4";
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessEngineConfiguration engConf = processEngine.getProcessEngineConfiguration();
        HistoryService historyService=processEngine.getHistoryService();
        // 获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        List<HistoricActivityInstance> highLightedActivityList =historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
        //绘制图片
        ProcessDiagramGenerator diagramGenerator = engConf.getProcessDiagramGenerator();
        InputStream resourceAsStream = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivityList.stream().map(hi -> hi.getActivityId()).collect(Collectors.toList()), new ArrayList<String>(), engConf.getActivityFontName(),
                engConf.getLabelFontName(), engConf.getAnnotationFontName(), engConf.getClassLoader(), 1.0, true);
        try {
            FileOutputStream image = new FileOutputStream("e:\\common_" + processInstanceId + ".png");
            byte[] bytes = new byte[10];
            int length = 0;
            while ((length = resourceAsStream.read(bytes)) != -1) {
                image.write(bytes, 0, length);
            }
            image.close();
            resourceAsStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取流程定义图片
     */
    @Test
    public void testProcessImage() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId("CommonProcess:1:4").singleResult();
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getDiagramResourceName());
        try {
            FileOutputStream image = new FileOutputStream("e:\\common.png");
            byte[] bytes = new byte[10];
            int length = 0;
            while ((length = resourceAsStream.read(bytes)) != -1) {
                image.write(bytes, 0, length);
            }
            image.close();
            resourceAsStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
