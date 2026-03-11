<template>
  <div class="cvs-page">
    <div class="hero cvs-card">
      <div class="left">
        <div class="h1">欢迎回来</div>
        <div class="h2 cvs-muted">在这里查看轮播图、热点信息、最新活动与热门帖子（后续接后端接口）。</div>
      </div>
      <div class="right">
        <el-button type="primary">去活动中心</el-button>
      </div>
    </div>

    <div class="grid">
      <div class="block cvs-card">
        <div class="title">轮播图</div>
        <div class="placeholder cvs-muted">Banner 列表占位</div>
      </div>
      <div class="block cvs-card">
        <div class="title">热点信息（Top5）</div>
        <div class="placeholder cvs-muted">News Top5 占位</div>
      </div>
      <div class="block cvs-card">
        <div class="title">最新志愿活动（Top4）</div>
        <div class="placeholder cvs-muted">Activity Top4 占位</div>
      </div>
      <div class="block cvs-card">
        <div class="title">热门帖子（Top6）</div>
        <div class="placeholder cvs-muted">Post Top6 占位</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.h1 {
  font-size: 18px;
  font-weight: 900;
}
.h2 {
  margin-top: 6px;
  font-size: 12px;
}
.grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.block {
  padding: 14px;
}
.title {
  font-weight: 900;
  margin-bottom: 10px;
}
.placeholder {
  height: 140px;
  display: grid;
  place-items: center;
  border: 1px dashed var(--cvs-border);
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.02);
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

