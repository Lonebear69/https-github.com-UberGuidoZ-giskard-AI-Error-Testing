<template>
    <div class="vertical-container">
        <v-container fluid class="vc" v-if="testSuitesStore.testSuites.length > 0">
            <div v-if="testSuitesStore.currentTestSuiteId === null">
                <v-row>
                    <v-col cols="4">
                        <v-text-field label="Search for a test suite" append-icon="search" outlined v-model="searchSession"></v-text-field>
                    </v-col>
                    <v-col cols="8">
                        <div class="d-flex justify-end">
                            <v-btn color="primaryLight" class="primaryLightBtn" @click="createTestSuite">
                                <v-icon left>add</v-icon>
                                New test suite
                            </v-btn>
                        </div>
                    </v-col>
                </v-row>

                <v-expansion-panels>
                    <v-row class="mr-12 ml-6 caption secondary--text text--lighten-3 pb-2">
                        <v-col cols="3">Suite name</v-col>
                        <v-col cols="2">Last execution</v-col>
                        <v-col cols="2">Tests info</v-col>
                        <v-col cols="2">Status</v-col>
                        <v-col cols="2">Total executions</v-col>
                        <v-col cols="1"></v-col>
                    </v-row>

                    <v-expansion-panel v-for="(suite, index) in filteredSuites" :key="suite.suite.id" @click.stop="openTestSuite(suite.suite.id)" class="expansion-panel">
                        <v-expansion-panel-header :disableIconRotate="true" class="grey lighten-5" tile>
                            <v-row class="px-2 py-1 align-center">
                                <v-col cols="3" class="font-weight-bold">
                                    <div class="pr-4">
                                        <InlineEditText :text="suite.suite.name" @save="(name) => renameSuite(suite.suite.id, name)">
                                        </InlineEditText>
                                    </div>
                                </v-col>
                                <v-col cols="2">
                                    <span v-if="latestExecutions[index]?.executionDate">{{ latestExecutions[index]?.executionDate | date }}</span>
                                    <span v-else>Never</span>
                                </v-col>
                                <v-col cols="2">
                                    <div class="d-flex flex-column">
                                        <span class="font-weight-bold">{{ suite.suite.tests.length }} tests in total</span>
                                        <div v-if="latestExecutions[index]?.executionDate" class="d-flex flex-column">
                                          <span class="passed-tests">{{
                                              latestExecutions[index]?.results?.filter(result => result.status === TestResult.PASSED).length
                                            }} passing</span>
                                          <span class="failed-tests">{{
                                              latestExecutions[index]?.results?.filter(result => result.status === TestResult.FAILED).length
                                            }} failing</span>
                                          <span class="error-tests">{{
                                              latestExecutions[index]?.results?.filter(result => result.status === TestResult.ERROR).length
                                            }} with error</span>
                                        </div>
                                    </div>
                                </v-col>
                                <v-col cols="2">
                                    <div v-if="latestExecutions[index]?.result === 'PASSED'" class="passed-tests font-weight-bold d-flex">
                                        <v-icon small class="mr-1 success-icon">done</v-icon>
                                        <span>PASSING</span>
                                    </div>
                                    <div v-else-if="latestExecutions[index]?.result === 'FAILED'" class="failed-tests font-weight-bold d-flex">
                                        <v-icon small class="mr-1 failed-icon">close</v-icon>
                                        <span>FAILING</span>
                                    </div>
                                    <span v-else class="font-weight-bold">NOT EXECUTED</span>
                                </v-col>
                                <v-col cols="2">
                                    <!-- total number of executions -->
                                    <span v-if="testSuitesStore.testSuitesComplete[index].executions.length">{{ testSuitesStore.testSuitesComplete[index].executions.length }} execution{{ testSuitesStore.testSuitesComplete[index].executions.length > 1 ? 's' : '' }}</span>
                                    <span v-else>None</span>
                                </v-col>
                                <v-col cols="1">
                                    <v-card-actions>
                                        <v-btn icon @click.stop.prevent="deleteTestSuite(suite.suite)">
                                            <v-icon color="accent">delete</v-icon>
                                        </v-btn>
                                    </v-card-actions>
                                </v-col>
                            </v-row>
                        </v-expansion-panel-header>
                    </v-expansion-panel>
                </v-expansion-panels>
            </div>
            <div v-else>
                <router-view />
            </div>

        </v-container>
        <v-container v-else-if="projectArtifactsStore.datasets.length > 0 && projectArtifactsStore.models.length > 0" class="vc mt-6 fill-height">
            <v-alert class="text-center">
                <p class="headline font-weight-medium grey--text text--darken-2">You haven't created any test suite for this project. <br>Please create a new one.</p>
            </v-alert>
            <v-btn tile @click="createTestSuite" color="primaryLight" class="primaryLightBtn">
                <v-icon>add</v-icon>
                Create a new test suite
            </v-btn>
            <div class="d-flex justify-center mb-6">
                <img src="@/assets/logo_test_suite.png" class="test-suite-logo" title="Test suite tab logo" alt="A turtle checking a to-do list">
            </div>
        </v-container>
        <v-container v-else-if="apiAccessToken && apiAccessToken.id_token">
            <div class="d-flex mb-6">
                <v-spacer></v-spacer>

                <div class="mr-2">
                    <v-btn @click="refresh">
                        Reload
                        <v-icon right>refresh</v-icon>
                    </v-btn>
                    <v-btn v-if="projectArtifactsStore.datasets.length > 0" color="primary" class="ml-2" @click="">
                        Upload with API
                        <v-icon right>mdi-application-braces-outline</v-icon>
                    </v-btn>
                </div>
            </div>
            <p class="font-weight-medium secondary--text">There are no artifacts (datasets and models) in this project yet. Follow the code snippet below to upload them and create a test suite 👇</p>
            <CodeSnippet :code-content="codeContent" :language="'python'"></CodeSnippet>
            <p class="mt-4 font-weight-medium secondary--text">Check out the <a href="https://docs.giskard.ai/en/latest/guides/scan/index.html" target="_blank" rel="noopener">full documentation</a> for more information.</p>
        </v-container>
    </div>
</template>

<script lang="ts" setup>
import { api } from '@/api';
import { apiURL } from '@/env';
import { computed, onActivated, ref } from 'vue';
import router from '@/router';
import { useMainStore } from '@/stores/main';
import { useTestSuitesStore } from '@/stores/test-suites';
import { TYPE } from 'vue-toastification';
import InlineEditText from '@/components/InlineEditText.vue';
import { $vfm } from 'vue-final-modal';
import ConfirmModal from '@/views/main/project/modals/ConfirmModal.vue';
import mixpanel from 'mixpanel-browser';
import CodeSnippet from '@/components/CodeSnippet.vue';
import { JWTToken, TestResult } from '@/generated-sources';
import { useProjectStore } from '@/stores/project';
import { useProjectArtifactsStore } from '@/stores/project-artifacts';

const projectStore = useProjectStore();
const testSuitesStore = useTestSuitesStore();
const projectArtifactsStore = useProjectArtifactsStore();

const props = defineProps<{
  projectId: number
}>();

const searchSession = ref('');
const apiAccessToken = ref<JWTToken | null>(null);

const codeContent = computed(() =>
    `import giskard

# for demo purposes only 🛳️. Replace with your dataframe creation
original_model, original_df = giskard.demo.titanic()

# Create a Giskard client
token = "${apiAccessToken.value?.id_token}"
client = giskard.GiskardClient(
    url="${apiURL}",  # URL of your Giskard instance
    token=token
)

# Wrap your model and dataset with Giskard 🎁
giskard_model = giskard.Model(original_model, model_type="classification", name="Titanic model")
giskard_dataset = giskard.Dataset(original_df, target="Survived", name="Titanic dataset")

# Scan your model for potential issues 🕵️
results = giskard.scan(giskard_model, giskard_dataset)

# Upload an automatically created test suite to the current project ✉️
results.generate_test_suite("Test suite created by scan").upload(client, "${project.value!.key}")
`
);

const project = computed(() => {
    return projectStore.project(props.projectId)
});

const filteredSuites = computed(() => {
    return testSuitesStore.testSuitesComplete.filter(suite => suite.suite.name.toLowerCase().includes(searchSession.value.toLowerCase()));
});

const latestExecutions = computed(() => {
    const executions = filteredSuites.value.map(suite => suite.executions);
    return executions.map(executionsSuite => executionsSuite.length === 0 ? null : executionsSuite[0]);
});

async function createTestSuite() {
    const project = await api.getProject(props.projectId)


    const suite = await api.createTestSuite(project.key, {
        id: null,
        name: `Unnamed test suite`,
        projectKey: project.key,
        functionInputs: [],
        tests: []
    });

    mixpanel.track('Create test suite',
        {
            id: suite,
            projectKey: project.key,
            screen: 'Test suites'
        }
    );

    await testSuitesStore.reload()
    await router.push({ name: 'test-suite-overview', params: { suiteId: suite.toString() } });
}

async function openTestSuite(suiteId: number | null | undefined) {
    if (!suiteId) {
        useMainStore().addNotification({
            content: 'Test suite not found',
            color: TYPE.ERROR
        });
    }

    testSuitesStore.setCurrentTestSuiteId(suiteId!);
    await redirectToTestSuite(props.projectId.toString(), suiteId!.toString());
}

async function redirectToTestSuite(projectId: string, suiteId: string) {
    await router.push({
        name: 'test-suite-overview',
        params: {
            projectId,
            suiteId
        }
    });
}

async function renameSuite(suiteId: number, name: string) {
    const currentSuite = testSuitesStore.testSuites.find(s => s.id === suiteId);

    if (currentSuite) {
        currentSuite.name = name;
        await testSuitesStore.updateSuiteName(currentSuite);
        mixpanel.track('Renamed test suite', { suiteId });
    }
}

function deleteTestSuite(suite: any) {
    $vfm.show({
        component: ConfirmModal,
        bind: {
            title: 'Delete test suite',
            text: `Are you sure that you want to delete the test suite '${suite.name}'?`,
            isWarning: true
        },
        on: {
            async confirm(close) {
                await api.deleteSuite(suite.projectKey!, suite.id!);
                await testSuitesStore.reloadComplete();
                close();
                useMainStore().addNotification({
                    content: `The test suite '${suite.name}' has been deleted.`,
                    color: TYPE.SUCCESS,
                    showProgress: false
                });

                mixpanel.track('Delete test suite',
                    {
                        id: suite.id,
                        projectKey: suite.projectKey,
                        screen: 'Test suites'
                    });
            }
        }
    });
}

const generateApiAccessToken = async () => {
    try {
        apiAccessToken.value = await api.getApiAccessToken();
    } catch (error) {
        console.log(error);
    }
}

async function refresh() {
    await testSuitesStore.reloadComplete();
    await projectArtifactsStore.loadProjectArtifacts(false);
}

onActivated(async () => {
    searchSession.value = "";
    if (testSuitesStore.currentTestSuiteId !== null) {
        await redirectToTestSuite(props.projectId.toString(), testSuitesStore.currentTestSuiteId.toString());
    } else {
        await testSuitesStore.loadTestSuites(props.projectId);
        await testSuitesStore.loadTestSuiteComplete(props.projectId);
        await projectArtifactsStore.setProjectId(props.projectId, false);
    }
    await generateApiAccessToken();
})
</script>

<style scoped>
.test-suite-logo {
    width: min(17.5vw, 150px);
    margin-top: 2rem;
}

.expansion-panel {
    margin-top: 10px !important;
}

.success-icon {
    color: #66AD5B;
}

.failed-icon {
    color: #EB5E59;
}

.passed-tests {
  margin-top: 0.25rem;
  color: #66AD5B;
}

.failed-tests {
  margin-top: 0.25rem;
  color: #EB5E59;
}

.error-tests {
  margin-top: 0.25rem;
  color: #ebba59;
}
</style>
