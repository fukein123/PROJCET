import { createApp } from 'vue'
import {
  ElAlert,
  ElAvatar,
  ElButton,
  ElCard,
  ElCarousel,
  ElCarouselItem,
  ElCollapse,
  ElCollapseItem,
  ElDatePicker,
  ElDialog,
  ElDrawer,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElLoading,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElTooltip,
  ElTimeline,
  ElTimelineItem,
  ElUpload,
  ElImage,
  ElEmpty
} from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
// Load project tokens and Element Plus overrides after the library defaults.
import '@/assets/styles/index.css'

const app = createApp(App)
const elementComponents = [
  ElAlert,
  ElAvatar,
  ElButton,
  ElCard,
  ElCarousel,
  ElCarouselItem,
  ElCollapse,
  ElCollapseItem,
  ElDatePicker,
  ElDialog,
  ElDrawer,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElTooltip,
  ElTimeline,
  ElTimelineItem,
  ElUpload,
  ElImage,
  ElEmpty
]

elementComponents.forEach((component) => {
  const c = component as any
  if (typeof c.install === 'function') {
    app.use(c)
    return
  }
  if (typeof c.name === 'string' && c.name.length > 0) {
    app.component(c.name, c)
  }
})
app.directive('loading', ElLoading.directive)
app.use(createPinia())
app.use(router)
app.mount('#app')
