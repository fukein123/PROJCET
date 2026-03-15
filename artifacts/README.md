# Artifacts Archive

This directory centralizes previously generated package/build outputs so they are easier to find.

## Source locations

- Backend jars are copied from `backend/target/`
- Frontend static build files are copied from `Frontend/dist/`

## Current contents

- `backend-1.0.0.jar`
- `backend-1.0.0.jar.original`
- `backend-2.0.0.jar`
- `backend-2.0.0.jar.original`
- `frontend-dist/`

## Notes

- Files here are copied from the original build directories. The originals are kept in place to avoid breaking existing tooling.
- Unless explicitly required, future work should avoid running real packaging again.
- If packaging is required later, put the outputs in this directory so they remain centralized.

## Reference documents for ongoing development

- `file/20-下一阶段功能补全开发计划.md`
- `file/21-社区志愿服务管理系统总结报告与优化计划.md`
