<script setup>
    import { reactive } from 'vue';
    import { useAuthStore } from '../store/auth';

    const auth = useAuthStore()

    const props = defineProps({
        fetchNewArticles: Function
    });

    const dialog = reactive({
        isVisible: false,
        selectedCategories: [],
        selectedLanguage: '',
        error: '',
        success : ''
    });

    const statusDialog = reactive({
        isVisible: false,
        message: '',
        status: ''
    })

    const allCategories = ['business','entertainment','health','science','sports','technology'];
    const allLanguages = ['en','de','fr','it','ru','tr'];

    const openDialog = async () => {

        try{

            const response = await fetch('http://localhost:8090/api/v1/user/news-preferences',{
                method : "GET",
                headers : {
                    "Authorization" : `Bearer ${auth.token}`
                }
            });

            if(response.status == 401) {
                auth.logout();
            }

            const parsedResponse = await response.json();

            if(response.status == 200) {
                dialog.selectedCategories = parsedResponse.interestedTopics;
                dialog.selectedLanguage = parsedResponse.language;

                dialog.isVisible = true;

            } else {

                throw new Error(parsedResponse.message)
        
            }

        } catch(e) {
            statusDialog.status = 'error';
            statusDialog.message = e;
            statusDialog.isVisible = true;
        }

    }

    const closeWithOk = async () => {
        
        try {

            dialog.isVisible = false;

            const response = await fetch('http://localhost:8090/api/v1/user/news-preferences',{
                method : "POST",
                headers : {
                    "Content-Type" : "application/json",
                    "Authorization" : `Bearer ${auth.token}`
                },
                body : JSON.stringify({
                    "language" : dialog.selectedLanguage,
                    "interestedTopics" : dialog.selectedCategories
                })
            })

            if(response.status == 401) {
                auth.logout();
            }           

            if(response.status == 200) {
                const successResponse = await response.text()

                statusDialog.status = 'success';
                statusDialog.message = successResponse;
                statusDialog.isVisible = true;

                props.fetchNewArticles();
            
            } else {
                const errorResponse = await response.json()

                throw new Error(errorResponse.message)
            }

        } catch(e) {
            statusDialog.status = 'error';
            statusDialog.message = e;
            statusDialog.isVisible = true;

        }

    }

    const closeStatusDialog = () => {
        statusDialog.isVisible = false;
        statusDialog.message = '';
        statusDialog.status = '';
    }
</script>
<template>
    <v-btn
        color="cyan-darken-4"
        variant="tonal"
        icon="mdi-cog"
        @click="openDialog">
    </v-btn>
    <v-dialog
        transition="dialog-bottom-transition"
        v-model="dialog.isVisible"
        width="auto"
    >
        <v-card
            width="400"
        >
            <v-toolbar
                color="cyan-darken-4"
                title="Choose Categories"
            ></v-toolbar>
            <div class="ma-4">
                <v-checkbox v-for="(category,index) in allCategories"
                    v-model="dialog.selectedCategories"
                    :label="category"
                    :value="category"
                    color="cyan-darken-4"
                    :key="index"
                    hide-details
                >
                </v-checkbox>
                <v-combobox
                    v-model="dialog.selectedLanguage"
                    :items="allLanguages"
                    label="Language"
                ></v-combobox>
            </div>            
            <v-card-actions class="justify-end">
                <v-btn
                    variant="text"
                    color="cyan-darken-4"
                    @click="dialog.isVisible = false"
                >Close
                </v-btn>
                <v-btn
                    variant="tonal"
                    color="cyan-darken-4"
                    @click="closeWithOk"
                >Ok
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
    <v-dialog 
        width="500"
        v-model="statusDialog.isVisible">
        <v-card :title="statusDialog.status === 'error' ? 'Error' : 'Success'">
            <v-card-text> 
                {{ statusDialog.message }}
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn
                    text="Close"
                    @click="closeStatusDialog"
                ></v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>