<template>
    <div class="vc mt-2 pb-0" v-if="transformationFunctions.length > 0">
        <div class="vc">
            <v-container class="main-container vc">
                <v-alert v-if="!hasGiskardFilters" color="warning" border="left" outlined colored-border icon="warning">
                    <span>Giskard filters are not available.</span>
                    <StartWorkerInstructions />
                </v-alert>
                <v-row class="fill-height">
                    <v-col cols="4" class="vc fill-height">
                        <v-text-field label="Search filter" append-icon="search" outlined v-model="searchFilter"></v-text-field>
                        <v-list three-line class="vc fill-height">
                            <v-list-item-group v-model="selected" color="primary" mandatory>
                                <template v-for="transformationFunction in filteredTestFunctions">
                                    <v-divider />
                                    <v-list-item :value="transformationFunction">
                                        <v-list-item-content>
                                            <v-list-item-title class="test-title">
                                                <div class="d-flex align-center">
                                                    <span class="list-func-name">
                                                        {{ transformationFunction.displayName ?? transformationFunction.name }}
                                                    </span>
                                                    <v-spacer class="flex-grow-1" />
                                                    <v-tooltip bottom v-if="transformationFunction.potentiallyUnavailable">
                                                        <template v-slot:activator="{ on, attrs }">
                                                            <div v-bind="attrs" v-on="on">
                                                                <v-icon color="orange">warning</v-icon>
                                                            </div>
                                                        </template>
                                                        <span>This filter is potentially unavailable. Start your external ML worker to display available filters.</span>
                                                    </v-tooltip>
                                                </div>
                                            </v-list-item-title>
                                            <v-list-item-subtitle v-if="transformationFunction.tags">
                                                <v-chip class="mr-2" v-for="tag in alphabeticallySorted(transformationFunction.tags)" x-small :color="pasterColor(tag)">
                                                    {{ tag }}
                                                </v-chip>
                                            </v-list-item-subtitle>
                                        </v-list-item-content>
                                    </v-list-item>
                                </template>
                            </v-list-item-group>
                        </v-list>
                    </v-col>
                    <v-col cols="8" v-if="selected" class="vc fill-height">
                        <div class="py-2">
                            <span class="selected-func-name">{{ selected.displayName ?? selected.name }}</span>
                        </div>

                        <div class="vc overflow-x-hidden pr-5">
                            <v-alert v-if="selected.potentiallyUnavailable" color="warning" border="left" outlined colored-border icon="warning">
                                <span>This filter is potentially unavailable. Start your external ML worker to display available filters.</span>
                                <pre></pre>
                                <StartWorkerInstructions />
                            </v-alert>

                            <div class="py-4" id="description-group">
                                <v-expansion-panels multiple v-model="panel" flat>
                                    <v-expansion-panel>
                                        <v-expansion-panel-header class="pl-0">
                                            <div class="d-flex align-center">
                                                <v-icon left class="group-icon pb-1 mr-1">mdi-text-box</v-icon>
                                                <span class="group-title">Description</span>
                                                <v-icon v-if="panel.includes(0)" right>mdi-chevron-up</v-icon>
                                                <v-icon v-else right>mdi-chevron-down</v-icon>
                                            </div>
                                            <template v-slot:actions>
                                                <span></span>
                                            </template>
                                        </v-expansion-panel-header>
                                        <v-expansion-panel-content>
                                            <p class="selected-func-description pt-2 mb-4">{{ doc.body }}</p>
                                        </v-expansion-panel-content>
                                    </v-expansion-panel>
                                </v-expansion-panels>
                            </div>

                            <div id="inputs-group" class="py-4 mb-4">
                                <div class="d-flex mb-4">
                                    <v-icon left class="group-icon pb-1 mr-1">mdi-pencil-box</v-icon>
                                    <span class="group-title">Inputs</span>
                                    <v-spacer></v-spacer>
                                </div>

                                <div>
                                    <v-row>
                                        <v-col>
                                            <span class="input-name">Dataset: <span class="input-type">BaseDataset</span></span>
                                        </v-col>
                                        <v-col class="input-selector-column">
                                            <DatasetSelector :project-id="projectId" label="Dataset" :return-object="false" :value.sync="selectedDataset" />
                                        </v-col>
                                    </v-row>
                                </div>

                                <div v-if="selected.cellLevel">
                                    <v-row>
                                        <v-col>
                                            <span class="input-name">Column: <span class="input-type">str</span></span>
                                        </v-col>
                                        <v-col class="input-selector-column">
                                            <DatasetColumnSelector :project-id="projectId" :dataset="selectedDataset" :column-type="selected.columnType" :value.sync="selectedColumn" />
                                        </v-col>
                                    </v-row>
                                </div>

                                <SuiteInputListSelector :editing="true" :model-value="transformationArguments" :inputs="inputType" :project-id="props.projectId" class="suite-input-list-selector" :doc="doc" />

                                <div class="d-flex">
                                    <v-spacer></v-spacer>
                                    <v-btn width="100" small class="primaryLightBtn" color="primaryLight" @click="runTransformationFunction">
                                        Run
                                    </v-btn>
                                </div>

                                <v-row v-if="transformationResult">
                                    <v-col>
                                        <span class="text-h6">Result</span>
                                        <p>Modified rows:
                                            {{
                                                transformationResult.modifications.length
                                            }} /
                                            {{
                                                transformationResult.totalRows
                                            }}</p>
                                        <DatasetTable :dataset-id="transformationResult.datasetId" :modifications="transformationResult.modifications" />
                                    </v-col>
                                </v-row>
                            </div>

                            <div id="code-group" class="py-4">
                                <div class="d-flex">
                                    <v-icon left class="group-icon pb-1 mr-1">mdi-code-braces-box</v-icon>
                                    <span class="group-title">Source code</span>
                                </div>
                                <CodeSnippet class="mt-2" :codeContent="selected.code" :key="selected.name + '_source_code'"></CodeSnippet>
                            </div>
                        </div>
                    </v-col>
                </v-row>
            </v-container>
        </div>
    </div>
    <v-container v-else class="d-flex flex-column vc fill-height">
        <h1 class="pt-16">ML Worker is not connected</h1>
        <StartWorkerInstructions />
    </v-container>
</template>

<script setup lang="ts">
import _, { chain } from "lodash";
import { computed, inject, onActivated, ref, watch } from "vue";
import { pasterColor } from "@/utils";
import { editor } from "monaco-editor";
import { FunctionInputDTO, TransformationFunctionDTO } from "@/generated-sources";
import StartWorkerInstructions from "@/components/StartWorkerInstructions.vue";
import { storeToRefs } from "pinia";
import { useCatalogStore } from "@/stores/catalog";
import DatasetSelector from "@/views/main/utils/DatasetSelector.vue";
import { api } from "@/api";
import DatasetTable from "@/components/DatasetTable.vue";
import SuiteInputListSelector from "@/components/SuiteInputListSelector.vue";
import DatasetColumnSelector from "@/views/main/utils/DatasetColumnSelector.vue";
import { alphabeticallySorted } from "@/utils/comparators";
import { extractArgumentDocumentation } from "@/utils/python-doc.utils";
import IEditorOptions = editor.IEditorOptions;
import CodeSnippet from "@/components/CodeSnippet.vue";
import mixpanel from "mixpanel-browser";
import { anonymize } from "@/utils";

let props = defineProps<{
    projectId: number,
    suiteId?: number
}>();

const editor = ref(null)

const searchFilter = ref<string>("");
let { transformationFunctions } = storeToRefs(useCatalogStore());
const selected = ref<TransformationFunctionDTO | null>(null);
const transformationResult = ref<FunctionInputDTO | null>(null);
const selectedDataset = ref<string | null>(null);
const selectedColumn = ref<string | null>(null);
let transformationArguments = ref<{ [name: string]: FunctionInputDTO }>({})

const monacoOptions: IEditorOptions = inject('monacoOptions');
const panel = ref<number[]>([0]);

monacoOptions.readOnly = true;

function resizeEditor() {
    setTimeout(() => {
        editor.value.editor.layout();
    })
}

const hasGiskardFilters = computed(() => {
    return transformationFunctions.value.find(t => t.tags.includes('giskard')) !== undefined
})

const filteredTestFunctions = computed(() => {
    return chain(transformationFunctions.value)
        .filter((func) => {
            const keywords = searchFilter.value.split(' ')
                .map(keyword => keyword.trim().toLowerCase())
                .filter(keyword => keyword !== '');

            return keywords.filter(keyword =>
                func.name.toLowerCase().includes(keyword)
                || func.doc?.toLowerCase()?.includes(keyword)
                || func.displayName?.toLowerCase()?.includes(keyword)
            ).length === keywords.length;
        })
        .sortBy(t => t.displayName ?? t.name)
        .value();
})

onActivated(async () => {
    if (transformationFunctions.value.length > 0) {
        selected.value = transformationFunctions.value[0];
    }
});

async function runTransformationFunction() {
    const params = Object.values(transformationArguments.value);
    if (selected.value!.cellLevel) {
        params.push({
            isAlias: false,
            name: 'column_name',
            params: [],
            type: 'str',
            value: selectedColumn.value
        })
    }

    mixpanel.track("Run transformation function from Catalog", {
        transformationFunctionName: selected.value!.name,
        inputs: anonymize(params),
    });

    transformationResult.value = await api.datasetProcessing(props.projectId, selectedDataset.value!, [{
        uuid: selected.value!.uuid,
        params,
        type: 'TRANSFORMATION'
    }]);
}

watch(() => selected.value, () => {
    transformationResult.value = null;

    if (!selected.value) {
        return;
    }

    transformationArguments.value = chain(selected.value.args)
        .keyBy('name')
        .mapValues(arg => ({
            name: arg.name,
            isAlias: false,
            type: arg.type,
            value: null
        }))
        .value()
})

const inputType = computed(() => chain(selected.value?.args ?? [])
    .keyBy('name')
    .mapValues('type')
    .value()
);

const doc = computed(() => extractArgumentDocumentation(selected.value));

</script>

<style scoped lang="scss">
.main-container {
    width: 100%;
    max-width: 100%;
}

.test-title {
    white-space: break-spaces;
}

.box-grow {
    flex: 1;
    background: green;
    padding: 5px;
    margin: 5px;
    min-height: 0;
}

::v-deep .v-expansion-panel-content__wrap {
    padding: 0;
}

.test-doc {
    white-space: break-spaces;
}

::v-deep .overflow-ellipsis {
    max-width: 200px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
}

.input-name {
    font-family: 'Roboto Mono', monospace;
    opacity: 0.875;
}

.input-type {
    font-size: 0.875rem;
    opacity: 0.875;
}

.input-column {
    width: 300px;
}

.selected-func-name {
    font-weight: 600;
    font-size: 1.75rem;
}

.selected-func-description {
    white-space: break-spaces;
}

.group-title {
    font-size: 1.25rem;
    font-weight: 500;
    letter-spacing: normal;
}

.group-icon {
    color: #087038;
    font-size: 1.25rem;
    margin-top: 0.3rem;
}

.custom-input {
    padding-top: 0.25rem;
    padding-bottom: 0.25rem;
}

.input-selector-column {
    padding-bottom: 1rem;
    padding-top: 0.25rem;
}

.list-func-name {
    font-weight: 500;
}

.suite-input-list-selector {
    padding-top: 0;
}
</style>
