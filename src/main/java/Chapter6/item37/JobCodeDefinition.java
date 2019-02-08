package Chapter6.item37;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobCodeDefinition {
    /**
     * 직군그룹코드
     */
    public enum JOB_GROUP {
        SERVICE("서비스", Arrays.asList(JOB.SERVICE_TM
                , JOB.SERVICE_CREW, JOB.SERVICE_SS)),
        MANAGEMENT_SUPPORT("경영지원", Arrays.asList(JOB.MANAGEMENT_SUPPORT_HR
                , JOB.MANAGEMENT_SUPPORT_FA, JOB.MANAGEMENT_SUPPORT_PS)),
        PRODUCTION_MANAGEMENT("생산관리", Arrays.asList(JOB.PRODUCTION_MANAGEMENT_PM
                , JOB.PRODUCTION_MANAGEMENT_QM, JOB.PRODUCTION_MANAGEMENT_FM)),
        EMPTY("없음", Collections.emptyList());

        JOB_GROUP(String name, List<JOB> jobList) {
            this.name = name;
            this.jobList = jobList;
        }

        private final String name;
        private final List<JOB> jobList;

        /**
         * 직무로 직군 찾기
         *
         * @author yhj0429
         */
        public static JOB_GROUP findByJob(JOB job) {
            return Arrays.stream(JOB_GROUP.values())
                    .filter(jobGroup -> jobGroup.hasJobCode(job))
                    .findAny()
                    .orElseGet(() -> EMPTY);
        }

        /**
         * 해당 직무 코드를 가지고 있는지
         *
         * @author yhj0429
         */
        public boolean hasJobCode(JOB jobCode) {
            return jobList.stream()
                    .anyMatch(job -> job == jobCode);
        }

        public List<JOB> getJobList() {
            return jobList;
        }
    }

    /**
     * 직무코드
     */
    public enum JOB {
        // 서비스
        SERVICE_TM("온라인서비스"), // Telemarketer
        SERVICE_CREW("특수서비스"),
        SERVICE_SS("대면서비스"), // Store Sales

        // 경영지원
        MANAGEMENT_SUPPORT_HR("인사/총무"), // Human Resource/General Affairs
        MANAGEMENT_SUPPORT_FA("재무/회계"), // Finance Account
        MANAGEMENT_SUPPORT_PS("기획/전략"), // Plan Strategy

        // 생산관리
        PRODUCTION_MANAGEMENT_PM("구매/조달"), // Purchase Management
        PRODUCTION_MANAGEMENT_QM("품질관리"), // Quality Management
        PRODUCTION_MANAGEMENT_FM("유통관리"), // Flow Management
        ;


        JOB(String name) {
            this.name = name;
        }

        private final String name;
    }

    public static void main(String[] args) {
        Map<String, List<JOB>> collect = Arrays.stream(JOB_GROUP.values())
                .collect(Collectors.toMap(JOB_GROUP::name, JOB_GROUP::getJobList));

        System.out.println(collect);
        System.out.println(JOB_GROUP.SERVICE.hasJobCode(JOB.SERVICE_TM));
        System.out.println(JOB_GROUP.SERVICE.hasJobCode(JOB.MANAGEMENT_SUPPORT_FA));
        System.out.println(JOB.MANAGEMENT_SUPPORT_FA + "이 속한 직군:" + JOB_GROUP.findByJob(JOB.MANAGEMENT_SUPPORT_FA));
    }
}
