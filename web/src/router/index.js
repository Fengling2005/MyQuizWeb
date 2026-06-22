import {createRouter, createWebHistory} from 'vue-router'
import AdminLayout from "@/views/layout/AdminLayout.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: getRoutes()
})

function getRoutes() {
    let defaultRoutes = [
        {
            path: '/admin',
            name: 'admin',
            component: AdminLayout,
            redirect: "/admin/home",
            children: [
                {
                    path: "home",
                    name: "admin-home",
                    component: () =>
                    import ('../views/admin/Home.vue')
                },
                {
                    path: 'editCurrentUser',
                    name: 'admin-editCurrentUser',
                    component: () =>
                        import ('../views/EditCurrentUser.vue')
                },
                {
                    path: 'editPassword',
                    name: 'admin-editPassword',
                    component: () =>
                        import ('../views/EditPassword.vue')
                },
                {
                    path: 'admin',
                    name: 'Admin',
                    component: () =>
                        import ('../views/admin/AdminManage.vue')
                },
                {
                    path: 'question',
                    name: 'Question',
                    component: () =>
                        import ('../views/admin/QuestionManage.vue')
                },
                {
                    path: 'practice',
                    name: 'Practice',
                    component: () =>
                        import ('../views/admin/Practice.vue')
                },
                {
                    path: 'marked',
                    name: 'Marked',
                    component: () =>
                        import ('../views/admin/MarkedQuestions.vue')
                },
                {
                    path: 'wrong',
                    name: 'Wrong',
                    component: () =>
                        import('@/views/admin/WrongQuestions.vue'),
                }
            ]
        },
        {
            path: "/login",
            name: "login",
            component: () =>
                import ('../views/Login.vue')
        }, {
            path: "/register",
            name: "register",
            component: () =>
                import ('../views/Register.vue')
        }, {
            path: "/retrievePassword",
            name: "front-retrievePassword",
            component: () =>
                import ('../views/RetrievePassword.vue')
        }];
    defaultRoutes.push({
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        meta: {
            name: ''
        },
        component: () => import ('../views/404.vue')
    })
    console.log('getDynamicRoutes', defaultRoutes)
    return defaultRoutes;
}

router.beforeEach((to, from, next) => {
    next();
});
export default router
