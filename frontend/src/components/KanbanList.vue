<script setup>

    const props = defineProps({
        title: String,
        plans: Object,
        plansUtils: Object
    });

</script>
<template>
    <div class="w-100">
        <div class="text-center text-cyan-darken-4">
            <h3 class="text-h5">{{props.title}}</h3>
        </div>
        <div v-if="plans.length">
            <v-card
                v-for="(plan) in plans"
                class="my-8 text-cyan-darken-4"
                :key="plan.id"
                @click="props.plansUtils.currentExpandedPlan() === plan.id ? props.plansUtils.hidePlan() : props.plansUtils.expandPlan(plan.id)"
                :ripple="false"
            >
                <v-card-item>
                    <div class="d-flex justify-space-between align-center">
                        <div class="w-60">
                            <h4>{{ plan.title }}</h4>
                            <div v-if="plan.tags.length" class="mt-5 w-100 d-flex flex-wrap ga-2">
                                <v-chip
                                    v-for="(tag,index) in plan.tags"
                                    color="primary"
                                    label
                                    :key="index"
                                >
                                    {{ tag }}
                                </v-chip>
                            </div>
                        </div>                        
                        <div class="d-flex">
                            <v-btn
                                v-if="plan.kanbanList !== 'TODO'"
                                color="cyan-darken-4"
                                variant="text"
                                icon="mdi-chevron-left"
                                @click.stop="props.plansUtils.changeList(plan.id, (plan.kanbanList === 'DONE' ? 'TODAY' : plan.kanbanList === 'TODAY' ? 'THIS_WEEK' : 'TODO'))">
                            </v-btn>
                            <v-btn
                                v-if="plan.kanbanList !== 'DONE'"
                                color="cyan-darken-4"
                                variant="text"
                                icon="mdi-chevron-right"
                                @click.stop="props.plansUtils.changeList(plan.id, (plan.kanbanList === 'TODO' ? 'THIS_WEEK' : plan.kanbanList === 'THIS_WEEK' ? 'TODAY' : 'DONE'))">
                            </v-btn>
                        </div>
                    </div>
                </v-card-item>
                <v-expand-transition>
                    <v-card
                        v-show="props.plansUtils.currentExpandedPlan() === plan.id"
                    >
                        <v-card-item>
                            <div :class="'d-flex align-baseline '+(plan.content ? 'justify-space-between' : 'justify-end')">
                                <div v-if="plan.content" class="w-60">
                                    {{ plan.content }}
                                </div>
                                <div>
                                    <div class="d-flex">
                                        <v-btn
                                            color="red-darken-4"
                                            variant="text"
                                            icon="mdi-delete"
                                            @click.stop="props.plansUtils.deletePlan(plan.id)"
                                            >
                                        </v-btn>
                                        <v-btn
                                            color="orange-darken-4"
                                            variant="text"
                                            icon="mdi-pencil"
                                            @click.stop="props.plansUtils.changePlan(plan.id)"
                                            >
                                        </v-btn>
                                    </div>
                                </div> 
                            </div>                           
                        </v-card-item>
                    </v-card>
                </v-expand-transition>
            </v-card>
        </div>
        <div v-else class="pt-8 text-center text-disabled">
            {{ `${props.title} list is empty` }}
        </div>
    </div>
</template>
<style scoped>
.tags-container > * + *{
    margin-left:8px;
}
.w-60 {
    width: 60%;
}
</style>