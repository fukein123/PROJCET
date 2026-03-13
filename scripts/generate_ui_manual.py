from __future__ import annotations

import shutil
from dataclasses import dataclass
from datetime import date
from pathlib import Path

import fitz
import pdfplumber
from pypdf import PdfReader
from reportlab.graphics.shapes import Circle, Drawing, Line, Rect, String
from reportlab.lib import colors
from reportlab.lib.colors import HexColor
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parents[1]
FILE_DIR = ROOT / "file"
TMP_PREVIEW_DIR = ROOT / "tmp" / "pdfs" / "ui-manual-preview"
MARKDOWN_PATH = FILE_DIR / "14-1-UI规范手册.md"
PDF_PATH = FILE_DIR / "14-2-UI规范手册.pdf"

FONT_BODY = "CVSBody"
FONT_BOLD = "CVSBold"

PRIMARY = HexColor("#1F7A54")
PRIMARY_STRONG = HexColor("#18583C")
PRIMARY_INK = HexColor("#0F3927")
ACCENT = HexColor("#EFC264")
TEXT_MAIN = HexColor("#1A2520")
TEXT_SUB = HexColor("#55645D")
TEXT_MUTED = HexColor("#73827B")
BORDER = HexColor("#D8DFD6")
SURFACE = HexColor("#FFFFFF")
BACKGROUND = HexColor("#F5F7F4")
BACKGROUND_SOFT = HexColor("#EEF3ED")
WARNING = HexColor("#D28A28")
INFO = HexColor("#5E7068")
DANGER = HexColor("#D54848")
WARNING_SOFT = HexColor("#FFF4DF")
INFO_SOFT = HexColor("#EDF1EF")
DANGER_SOFT = HexColor("#FDECEB")
PRIMARY_SOFT = HexColor("#E4F1EA")
ADMIN_START = HexColor("#5A1326")
ADMIN_END = HexColor("#300713")
VOLUNTEER_START = HexColor("#0F4D35")
VOLUNTEER_END = HexColor("#0A2B1F")
PORTAL_START = HexColor("#134F36")
PORTAL_END = HexColor("#2A7A5F")


@dataclass(frozen=True)
class TableRow:
    name: str
    token: str
    value: str
    usage: str


DESIGN_PRINCIPLES = [
    ("唯一业务核心", "所有视觉表达都围绕社区志愿服务管理，不引入与主业务无关的娱乐化样式。"),
    ("统一而分区", "门户、志愿者、管理员共用同一套 token 和组件规范，通过角色色带区分，不重新发明一套 UI。"),
    ("服务优先", "页面层级、按钮强调、反馈文案都优先帮助用户完成报名、审核、签到签退、内容维护等任务。"),
    ("稳定可维护", "禁止页面内散落硬编码样式，统一消费 tokens、Element Plus 覆盖层和共享组件。"),
]

ROLE_TONES = [
    TableRow("门户 tone", "WorkspaceHero.portal", "#134F36 -> #2A7A5F", "公共信息入口、活动列表、论坛与公告"),
    TableRow("志愿者工作台", "MainLayout.role-volunteer", "#0F4D35 -> #0A2B1F", "任务执行、报名记录、打卡记录、个人中心"),
    TableRow("管理员工作台", "MainLayout.role-admin", "#5A1326 -> #300713", "审核、内容管理、用户管理、周统计"),
]

COLOR_ROWS = [
    TableRow("背景底色", "--cvs-color-bg", "#F5F7F4", "应用背景、浅色页面底板"),
    TableRow("柔和底色", "--cvs-color-bg-soft", "#EEF3ED", "弱层级分区、轻背景卡片"),
    TableRow("卡片表面", "--cvs-color-surface", "#FFFFFF", "卡片、表单、弹层"),
    TableRow("边框", "--cvs-color-border", "#D8DFD6", "卡片边界、表格分隔、输入框边框"),
    TableRow("正文", "--cvs-color-text-main", "#1A2520", "页面标题、正文、表头"),
    TableRow("次级文本", "--cvs-color-text-sub", "#55645D", "说明文字、副标题、辅助文案"),
    TableRow("弱文本", "--cvs-color-text-muted", "#73827B", "占位、次要提示、说明标签"),
    TableRow("品牌主色", "--cvs-color-primary", "#1F7A54", "主按钮、焦点、完成态强调"),
    TableRow("品牌深色", "--cvs-color-primary-strong", "#18583C", "深色按钮、重要 hover 状态"),
    TableRow("品牌墨色", "--cvs-color-primary-ink", "#0F3927", "深色背景、品牌压重区"),
    TableRow("暖金强调", "--cvs-color-accent", "#EFC264", "门户 hero 光感、亮点数字、装饰强调"),
    TableRow("警告色", "--cvs-color-warning", "#D28A28", "提醒、待处理、次高风险"),
    TableRow("信息色", "--cvs-color-info", "#5E7068", "中性状态、信息提示"),
    TableRow("危险色", "--cvs-color-danger", "#D54848", "删除、拒绝、错误"),
]

SOFT_COLOR_ROWS = [
    TableRow("主色浅层", "--cvs-color-primary-soft", "#E4F1EA", "轻提示条、成功背景、卡片装饰层"),
    TableRow("警告浅层", "--cvs-color-warning-soft", "#FFF4DF", "待处理提醒背景"),
    TableRow("信息浅层", "--cvs-color-info-soft", "#EDF1EF", "中性说明块"),
    TableRow("危险浅层", "--cvs-color-danger-soft", "#FDECEB", "危险提示背景、错误反馈面"),
]

TYPOGRAPHY_ROWS = [
    TableRow("字体族", "--cvs-font-family-sans", "Manrope / Noto Sans SC / Microsoft YaHei", "英文标题用 Manrope，中文正文回落到 Noto Sans SC / 微软雅黑"),
    TableRow("XS", "--cvs-font-size-xs", "12px", "辅助标签、eyebrow、状态说明"),
    TableRow("SM", "--cvs-font-size-sm", "13px", "说明文本、表格辅助信息"),
    TableRow("MD", "--cvs-font-size-md", "14px", "默认正文、表单输入、按钮文本"),
    TableRow("LG", "--cvs-font-size-lg", "16px", "模块标题、列表头"),
    TableRow("XL", "--cvs-font-size-xl", "22px", "区块标题"),
    TableRow("2XL", "--cvs-font-size-2xl", "28px", "重点数据卡、强指标"),
    TableRow("Medium", "--cvs-font-weight-medium", "500", "普通强调"),
    TableRow("Semibold", "--cvs-font-weight-semibold", "600", "标签、表单标题"),
    TableRow("Bold", "--cvs-font-weight-bold", "700", "按钮、表头"),
    TableRow("Heavy", "--cvs-font-weight-heavy", "800", "区块标题、品牌标题、关键数字"),
]

FOUNDATION_ROWS = [
    TableRow("间距", "--cvs-space-1 ~ 8", "4 / 8 / 12 / 16 / 20 / 24 / 32px", "页面、卡片、表单、按钮间距统一使用 4px 基数"),
    TableRow("圆角", "--cvs-radius-sm ~ pill", "8 / 12 / 16 / 20 / 999px", "输入控件 12px，卡片 16px，弹层 20px，胶囊标签 999px"),
    TableRow("动效", "--cvs-motion-fast / base", "0.15s / 0.22s", "按钮 hover、抽屉过渡、列表进入动画"),
    TableRow("缓动", "--cvs-ease-standard", "cubic-bezier(0.22, 1, 0.36, 1)", "统一加速与减速节奏"),
    TableRow("阴影", "--cvs-shadow-soft", "0 14px 30px rgba(24, 43, 34, 0.08)", "卡片、弹层、hero 统一投影"),
    TableRow("焦点环", "--cvs-shadow-focus-ring", "0 0 0 3px rgba(31, 122, 84, 0.14)", "输入框、按钮、日期选择器聚焦态"),
]

COMPONENT_ROWS = [
    TableRow("BrandMark", "BrandMark.vue", "品牌标识组件", "图标容器默认 42px / 14px 圆角；compact 缩小到 36px；tone-light 用于深色侧边栏"),
    TableRow("WorkspaceHero", "WorkspaceHero.vue", "门户与工作台页头", "两栏布局；默认 28px 内边距、24px 圆角；portal / volunteer 两套色带"),
    TableRow("StatePanel", "StatePanel.vue", "空态与加载态", "统一虚线边框、圆形状态徽记；loading 与 empty 共用版式"),
    TableRow("SearchForm", "SearchForm.vue", "筛选区标准模板", "固定字段区 + 操作区；操作按钮最小宽度 88px；960px 以下换行堆叠"),
    TableRow("AdminListScaffold", "AdminListScaffold.vue", "管理端列表页骨架", "固定头部、操作区、内容区、分页区结构，统一管理页结构"),
    TableRow("Element Plus 覆盖层", "element-overrides.css", "系统级组件皮肤", "按钮、输入框、表格、分页、弹窗统一消费同一套 token"),
]

INTERACTION_ROWS = [
    TableRow("按钮", "theme.css / element-overrides.css", "主按钮用品牌主色", "hover 统一上浮 1px；非 text/link 按钮带轻阴影"),
    TableRow("表单焦点", "element-overrides.css", "焦点环统一为绿色 3px", "输入、选择器、日期、数字输入统一聚焦反馈"),
    TableRow("危险操作", "confirmed-action.ts", "统一走确认弹窗", "删除、拒绝、批量删除等动作必须二次确认，必要时使用 prompt 输入理由"),
    TableRow("空态与加载", "StatePanel.vue", "统一状态组件", "避免页面自行拼装空态和 loading 提示"),
    TableRow("反馈信息", "request.ts", "成功走正反馈，异常走统一错误模板", "错误提示与鉴权失效跳转由请求层统一处理"),
]

BREAKPOINT_ROWS = [
    TableRow("960px", "SearchForm / AdminListScaffold", "筛选栏与操作区纵向堆叠", "管理页在平板宽度保留完整信息但避免挤压"),
    TableRow("900px", "MainLayout", "侧边栏隐藏，顶部品牌显式出现", "工作台切换到单列内容布局"),
    TableRow("720px", "MainLayout", "顶部栏多行换行", "右侧账号区和跳转操作自动折行"),
    TableRow("640px", "WorkspaceHero / BrandMark", "hero 按钮撑满宽度，副标题隐藏", "手机端优先保证主要动作与标题阅读"),
]

SOURCE_FILES = [
    "Frontend/src/assets/styles/tokens.css",
    "Frontend/src/assets/styles/theme.css",
    "Frontend/src/assets/styles/element-overrides.css",
    "Frontend/src/components/shared/BrandMark.vue",
    "Frontend/src/components/shared/WorkspaceHero.vue",
    "Frontend/src/components/shared/StatePanel.vue",
    "Frontend/src/components/SearchForm.vue",
    "Frontend/src/components/admin/AdminListScaffold.vue",
    "Frontend/src/layouts/MainLayout.vue",
]


def register_fonts() -> None:
    pdfmetrics.registerFont(TTFont(FONT_BODY, r"C:\Windows\Fonts\msyh.ttc"))
    pdfmetrics.registerFont(TTFont(FONT_BOLD, r"C:\Windows\Fonts\msyhbd.ttc"))


def table_data(headers: list[str], rows: list[TableRow]) -> list[list[str]]:
    return [headers] + [[row.name, row.token, row.value, row.usage] for row in rows]


def markdown_table(headers: list[str], rows: list[TableRow]) -> str:
    divider = "|" + "|".join([" --- " for _ in headers]) + "|"
    header = "|" + "|".join(headers) + "|"
    body = ["|" + "|".join([row.name, row.token, row.value, row.usage]) + "|" for row in rows]
    return "\n".join([header, divider, *body])


def build_markdown() -> str:
    sections = [
        "# UI 规范手册",
        "",
        f"更新时间：{date.today().isoformat()}",
        "",
        "## 1. 目的与范围",
        "",
        "- 适用对象：产品、前端、联调、验收。",
        "- 适用范围：社区志愿服务平台门户端、志愿者端、管理员端。",
        "- 手册目标：把现有统一风格固化为可执行标准，避免后续页面再次出现硬编码样式、视觉割裂或交互漂移。",
        "",
        "## 2. 设计核心",
        "",
        "\n".join([f"- {title}：{description}" for title, description in DESIGN_PRINCIPLES]),
        "",
        "### 2.1 角色分区色带",
        "",
        markdown_table(["区域", "来源", "色值", "使用范围"], ROLE_TONES),
        "",
        "## 3. 设计令牌",
        "",
        "### 3.1 主色板",
        "",
        markdown_table(["名称", "Token", "值", "用途"], COLOR_ROWS),
        "",
        "### 3.2 语义浅层色",
        "",
        markdown_table(["名称", "Token", "值", "用途"], SOFT_COLOR_ROWS),
        "",
        "### 3.3 字体层级",
        "",
        markdown_table(["名称", "Token", "值", "用途"], TYPOGRAPHY_ROWS),
        "",
        "### 3.4 间距、圆角、动效",
        "",
        markdown_table(["名称", "Token", "值", "用途"], FOUNDATION_ROWS),
        "",
        "## 4. 组件规范",
        "",
        markdown_table(["组件", "来源", "定位", "规范"], COMPONENT_ROWS),
        "",
        "### 4.1 状态与交互",
        "",
        markdown_table(["对象", "来源", "规范", "说明"], INTERACTION_ROWS),
        "",
        "## 5. 页面模板",
        "",
        "### 5.1 门户内容页",
        "",
        "- 结构：品牌导航 -> WorkspaceHero -> 内容卡片区。",
        "- 要点：使用 portal 色带，强调公共信息和活动入口，避免后台感侧边栏。",
        "",
        "### 5.2 志愿者工作台页",
        "",
        "- 结构：MainLayout 深绿色侧边栏 -> 顶栏标题区 -> 内容区。",
        "- 要点：突出任务执行效率，保持清晰的数据与操作路径。",
        "",
        "### 5.3 管理端列表页",
        "",
        "- 结构：SearchForm -> AdminListScaffold -> 表格 -> 分页。",
        "- 要点：所有管理型列表优先复用统一骨架，避免各页面自定义头部和分页风格。",
        "",
        "## 6. 响应式规范",
        "",
        markdown_table(["断点", "来源", "变化", "目标"], BREAKPOINT_ROWS),
        "",
        "## 7. 源文件映射",
        "",
        "\n".join([f"- `{path}`" for path in SOURCE_FILES]),
        "",
        "## 8. 执行要求",
        "",
        "- 新页面优先消费 token 和共享组件，不新增平行样式体系。",
        "- 禁止直接改写 Element Plus 默认变量而不经过 `element-overrides.css`。",
        "- 禁止在页面中新增与社区志愿服务管理主线无关的主题色或装饰语汇。",
        "- 新增危险操作必须接入统一确认与反馈链路。",
        "",
    ]
    return "\n".join(sections) + "\n"


def build_styles():
    sample = getSampleStyleSheet()
    return {
        "body": ParagraphStyle(
            "Body",
            parent=sample["BodyText"],
            fontName=FONT_BODY,
            fontSize=10.5,
            leading=17,
            textColor=TEXT_MAIN,
            spaceAfter=6,
        ),
        "small": ParagraphStyle(
            "Small",
            parent=sample["BodyText"],
            fontName=FONT_BODY,
            fontSize=8.5,
            leading=12,
            textColor=TEXT_SUB,
            spaceAfter=4,
        ),
        "h1": ParagraphStyle(
            "H1",
            parent=sample["Heading1"],
            fontName=FONT_BOLD,
            fontSize=26,
            leading=32,
            textColor=colors.white,
            spaceAfter=8,
        ),
        "h2": ParagraphStyle(
            "H2",
            parent=sample["Heading2"],
            fontName=FONT_BOLD,
            fontSize=17,
            leading=24,
            textColor=PRIMARY_INK,
            spaceAfter=8,
            spaceBefore=8,
        ),
        "h3": ParagraphStyle(
            "H3",
            parent=sample["Heading3"],
            fontName=FONT_BOLD,
            fontSize=12.5,
            leading=18,
            textColor=TEXT_MAIN,
            spaceAfter=6,
            spaceBefore=4,
        ),
        "cover_subtitle": ParagraphStyle(
            "CoverSubtitle",
            parent=sample["BodyText"],
            fontName=FONT_BODY,
            fontSize=11,
            leading=18,
            textColor=colors.white,
            spaceAfter=4,
        ),
        "chip": ParagraphStyle(
            "Chip",
            parent=sample["BodyText"],
            fontName=FONT_BOLD,
            fontSize=9,
            leading=12,
            textColor=PRIMARY_INK,
        ),
    }


def make_info_table(rows: list[TableRow], headers: list[str], col_widths: list[float]) -> Table:
    data = table_data(headers, rows)
    table = Table(data, colWidths=col_widths, repeatRows=1)
    style = TableStyle(
        [
            ("FONTNAME", (0, 0), (-1, 0), FONT_BOLD),
            ("FONTNAME", (0, 1), (-1, -1), FONT_BODY),
            ("FONTSIZE", (0, 0), (-1, 0), 9.5),
            ("FONTSIZE", (0, 1), (-1, -1), 8.8),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("BACKGROUND", (0, 0), (-1, 0), PRIMARY_STRONG),
            ("TEXTCOLOR", (0, 1), (-1, -1), TEXT_MAIN),
            ("ROWBACKGROUNDS", (0, 1), (-1, -1), [SURFACE, BACKGROUND_SOFT]),
            ("GRID", (0, 0), (-1, -1), 0.5, BORDER),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 8),
            ("RIGHTPADDING", (0, 0), (-1, -1), 8),
            ("TOPPADDING", (0, 0), (-1, -1), 7),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ]
    )
    table.setStyle(style)
    return table


def make_color_table(rows: list[TableRow], col_widths: list[float]) -> Table:
    headers = ["色板", "名称", "Token", "值", "用途"]
    data = [headers] + [["", row.name, row.token, row.value, row.usage] for row in rows]
    table = Table(data, colWidths=col_widths, repeatRows=1)
    style = TableStyle(
        [
            ("FONTNAME", (0, 0), (-1, 0), FONT_BOLD),
            ("FONTNAME", (0, 1), (-1, -1), FONT_BODY),
            ("FONTSIZE", (0, 0), (-1, 0), 9.5),
            ("FONTSIZE", (0, 1), (-1, -1), 8.8),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("BACKGROUND", (0, 0), (-1, 0), PRIMARY_STRONG),
            ("ROWBACKGROUNDS", (1, 1), (-1, -1), [SURFACE, BACKGROUND_SOFT]),
            ("GRID", (0, 0), (-1, -1), 0.5, BORDER),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 8),
            ("RIGHTPADDING", (0, 0), (-1, -1), 8),
            ("TOPPADDING", (0, 0), (-1, -1), 7),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ]
    )

    for index, row in enumerate(rows, start=1):
        style.add("BACKGROUND", (0, index), (0, index), HexColor(row.value.split(" ")[0]))

    table.setStyle(style)
    return table


def make_cover(width: float, styles: dict[str, ParagraphStyle]) -> Table:
    content = [
        Paragraph("社区志愿服务平台 UI 规范手册", styles["h1"]),
        Paragraph("统一风格、页面模板、组件状态与交互规则的执行基线", styles["cover_subtitle"]),
        Paragraph("基于当前 Vue 3 + Element Plus 前端实现整理", styles["cover_subtitle"]),
        Spacer(1, 8),
        Table(
            [[Paragraph("社区志愿服务管理", styles["chip"]), Paragraph("前后端分离", styles["chip"]), Paragraph("统一设计语言", styles["chip"])]],
            colWidths=[44 * mm, 34 * mm, 44 * mm],
            style=TableStyle(
                [
                    ("BACKGROUND", (0, 0), (-1, -1), ACCENT),
                    ("BOX", (0, 0), (-1, -1), 0, ACCENT),
                    ("INNERGRID", (0, 0), (-1, -1), 0, ACCENT),
                    ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                    ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                    ("LEFTPADDING", (0, 0), (-1, -1), 10),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                    ("TOPPADDING", (0, 0), (-1, -1), 6),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
                ]
            ),
        ),
        Spacer(1, 8),
        Paragraph(f"更新日期：{date.today().isoformat()}", styles["cover_subtitle"]),
    ]

    table = Table(
        [[content]],
        colWidths=[width],
        style=TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, -1), PRIMARY),
                ("BOX", (0, 0), (-1, -1), 0, PRIMARY),
                ("LEFTPADDING", (0, 0), (-1, -1), 22),
                ("RIGHTPADDING", (0, 0), (-1, -1), 22),
                ("TOPPADDING", (0, 0), (-1, -1), 24),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 24),
            ]
        ),
    )
    return table


def make_tone_cards(width: float) -> Table:
    cards = [
        ("门户", "#134F36 -> #2A7A5F", "公共信息入口、服务页头、活动与论坛"),
        ("志愿者", "#0F4D35 -> #0A2B1F", "任务执行、记录查看、个人工作台"),
        ("管理员", "#5A1326 -> #300713", "审核、治理、统计、批量操作"),
    ]
    rows: list[list[object]] = [[]]
    cell_width = (width - 18) / 3

    for title, tone, desc in cards:
        card = Table(
            [[Paragraph(f"<b>{title}</b><br/>{tone}<br/>{desc}", ParagraphStyle("ToneCard", fontName=FONT_BODY, fontSize=9.4, leading=15, textColor=TEXT_MAIN))]],
            colWidths=[cell_width],
            style=TableStyle(
                [
                    ("BACKGROUND", (0, 0), (-1, -1), SURFACE),
                    ("BOX", (0, 0), (-1, -1), 0.7, BORDER),
                    ("LEFTPADDING", (0, 0), (-1, -1), 10),
                    ("RIGHTPADDING", (0, 0), (-1, -1), 10),
                    ("TOPPADDING", (0, 0), (-1, -1), 10),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 10),
                ]
            ),
        )
        rows[0].append(card)

    table = Table(rows, colWidths=[cell_width, cell_width, cell_width], style=TableStyle([("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 9)]))
    return table


def make_state_showcase(width: float) -> Drawing:
    drawing = Drawing(width, 130)

    drawing.add(String(0, 118, "组件状态示例", fontName=FONT_BOLD, fontSize=12, fillColor=TEXT_MAIN))

    # Buttons
    button_y = 80
    specs = [
        (PRIMARY, None, colors.white, "主按钮"),
        (SURFACE, BORDER, TEXT_MAIN, "次按钮"),
        (DANGER, None, colors.white, "危险按钮"),
    ]
    x = 0
    for fill, stroke, text_color, label in specs:
        drawing.add(Rect(x, button_y, 78, 28, rx=10, ry=10, fillColor=fill, strokeColor=stroke or fill))
        drawing.add(String(x + 18, button_y + 9, label, fontName=FONT_BODY, fontSize=9, fillColor=text_color))
        x += 92

    # Inputs
    input_y = 34
    drawing.add(Rect(0, input_y, 120, 28, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
    drawing.add(String(10, input_y + 9, "默认输入框", fontName=FONT_BODY, fontSize=9, fillColor=TEXT_MUTED))

    drawing.add(Rect(140, input_y, 120, 28, rx=10, ry=10, fillColor=SURFACE, strokeColor=PRIMARY))
    drawing.add(Rect(137, input_y - 3, 126, 34, rx=12, ry=12, fillColor=None, strokeColor=HexColor("#7DB59A"), strokeWidth=2))
    drawing.add(String(150, input_y + 9, "焦点状态", fontName=FONT_BODY, fontSize=9, fillColor=TEXT_MAIN))

    drawing.add(Rect(280, input_y, 120, 28, rx=10, ry=10, fillColor=BACKGROUND_SOFT, strokeColor=BORDER))
    drawing.add(String(290, input_y + 9, "禁用状态", fontName=FONT_BODY, fontSize=9, fillColor=TEXT_MUTED))

    # Table strip
    table_x = width - 150
    drawing.add(Rect(table_x, 26, 145, 80, rx=14, ry=14, fillColor=SURFACE, strokeColor=BORDER))
    drawing.add(Rect(table_x, 84, 145, 22, fillColor=HexColor("#F6F8F4"), strokeColor=None))
    drawing.add(Line(table_x + 48, 26, table_x + 48, 106, strokeColor=BORDER))
    drawing.add(Line(table_x + 104, 26, table_x + 104, 106, strokeColor=BORDER))
    drawing.add(Line(table_x, 64, table_x + 145, 64, strokeColor=BORDER))
    drawing.add(Line(table_x, 46, table_x + 145, 46, strokeColor=BORDER))
    drawing.add(String(table_x + 10, 91, "表头", fontName=FONT_BOLD, fontSize=8, fillColor=TEXT_MAIN))
    drawing.add(String(table_x + 56, 91, "状态", fontName=FONT_BOLD, fontSize=8, fillColor=TEXT_MAIN))
    drawing.add(String(table_x + 110, 91, "操作", fontName=FONT_BOLD, fontSize=8, fillColor=TEXT_MAIN))
    drawing.add(String(table_x + 10, 71, "活动 A", fontName=FONT_BODY, fontSize=8, fillColor=TEXT_MAIN))
    drawing.add(String(table_x + 56, 71, "进行中", fontName=FONT_BODY, fontSize=8, fillColor=PRIMARY))

    return drawing


def make_template_showcase(width: float) -> Drawing:
    drawing = Drawing(width, 255)
    drawing.add(String(0, 242, "页面模板线框", fontName=FONT_BOLD, fontSize=12, fillColor=TEXT_MAIN))

    card_width = (width - 24) / 3
    templates = [
        ("门户内容页", PORTAL_START, "导航 / Hero / 卡片网格"),
        ("志愿者工作台", VOLUNTEER_START, "侧栏 / 顶栏 / 内容区"),
        ("管理端列表页", ADMIN_START, "筛选 / 头部 / 表格 / 分页"),
    ]

    for index, (title, accent, desc) in enumerate(templates):
        x = index * (card_width + 12)
        drawing.add(Rect(x, 12, card_width, 214, rx=16, ry=16, fillColor=SURFACE, strokeColor=BORDER))
        drawing.add(Rect(x, 188, card_width, 38, rx=16, ry=16, fillColor=accent, strokeColor=accent))
        drawing.add(String(x + 12, 204, title, fontName=FONT_BOLD, fontSize=10, fillColor=colors.white))
        drawing.add(String(x + 12, 176, desc, fontName=FONT_BODY, fontSize=8.2, fillColor=TEXT_SUB))

        if index == 0:
            drawing.add(Rect(x + 12, 136, card_width - 24, 28, rx=10, ry=10, fillColor=PRIMARY_SOFT, strokeColor=None))
            drawing.add(Rect(x + 12, 84, card_width - 24, 40, rx=10, ry=10, fillColor=BACKGROUND_SOFT, strokeColor=BORDER))
            drawing.add(Rect(x + 12, 28, (card_width - 30) / 2, 42, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
            drawing.add(Rect(x + 18 + (card_width - 30) / 2, 28, (card_width - 30) / 2, 42, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
        elif index == 1:
            drawing.add(Rect(x + 12, 28, 36, 146, rx=10, ry=10, fillColor=VOLUNTEER_END, strokeColor=None))
            drawing.add(Rect(x + 58, 142, card_width - 70, 32, rx=10, ry=10, fillColor=BACKGROUND_SOFT, strokeColor=BORDER))
            drawing.add(Rect(x + 58, 28, card_width - 70, 102, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
        else:
            drawing.add(Rect(x + 12, 146, card_width - 24, 28, rx=10, ry=10, fillColor=BACKGROUND_SOFT, strokeColor=BORDER))
            drawing.add(Rect(x + 12, 108, card_width - 24, 26, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
            drawing.add(Rect(x + 12, 46, card_width - 24, 52, rx=10, ry=10, fillColor=SURFACE, strokeColor=BORDER))
            drawing.add(Rect(x + card_width - 72, 20, 60, 16, rx=8, ry=8, fillColor=BACKGROUND_SOFT, strokeColor=BORDER))

    return drawing


def add_page_decor(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(PRIMARY)
    canvas.setLineWidth(0.7)
    canvas.line(doc.leftMargin, 12 * mm, A4[0] - doc.rightMargin, 12 * mm)
    canvas.setFont(FONT_BODY, 8)
    canvas.setFillColor(TEXT_SUB)
    canvas.drawString(doc.leftMargin, 8 * mm, "社区志愿服务平台 UI 规范手册")
    canvas.drawRightString(A4[0] - doc.rightMargin, 8 * mm, f"{canvas.getPageNumber()}")
    canvas.restoreState()


def build_pdf() -> None:
    register_fonts()
    styles = build_styles()
    doc = SimpleDocTemplate(
        str(PDF_PATH),
        pagesize=A4,
        leftMargin=16 * mm,
        rightMargin=16 * mm,
        topMargin=16 * mm,
        bottomMargin=18 * mm,
        title="社区志愿服务平台 UI 规范手册",
        author="OpenAI Codex",
    )

    story = [
        make_cover(doc.width, styles),
        Spacer(1, 10),
        Paragraph("设计总览", styles["h2"]),
        Paragraph("本手册是当前统一风格升级的执行基线，覆盖色板、字体层级、组件状态、页面模板与交互规范。后续新增页面必须优先复用现有 token、覆盖层和共享组件。", styles["body"]),
        make_tone_cards(doc.width),
        Spacer(1, 10),
        Paragraph("设计原则", styles["h2"]),
    ]

    for title, description in DESIGN_PRINCIPLES:
        story.append(Paragraph(f"<b>{title}</b>：{description}", styles["body"]))

    story.extend(
        [
            Spacer(1, 10),
            Paragraph("主色板", styles["h2"]),
            make_color_table(COLOR_ROWS, [14 * mm, 25 * mm, 42 * mm, 24 * mm, doc.width - 105 * mm]),
            Spacer(1, 10),
            Paragraph("语义浅层色", styles["h3"]),
            make_color_table(SOFT_COLOR_ROWS, [14 * mm, 25 * mm, 42 * mm, 24 * mm, doc.width - 105 * mm]),
            Spacer(1, 12),
            Paragraph("字体层级", styles["h2"]),
            make_info_table(TYPOGRAPHY_ROWS, ["名称", "Token", "值", "用途"], [25 * mm, 42 * mm, 38 * mm, doc.width - 105 * mm]),
            Spacer(1, 12),
            Paragraph("间距、圆角与动效", styles["h2"]),
            make_info_table(FOUNDATION_ROWS, ["名称", "Token", "值", "用途"], [22 * mm, 38 * mm, 52 * mm, doc.width - 112 * mm]),
            Spacer(1, 12),
            Paragraph("组件规范", styles["h2"]),
            make_info_table(COMPONENT_ROWS, ["组件", "来源", "定位", "规范"], [28 * mm, 38 * mm, 32 * mm, doc.width - 98 * mm]),
            Spacer(1, 12),
            make_state_showcase(doc.width),
            Spacer(1, 12),
            Paragraph("交互规范", styles["h2"]),
            make_info_table(INTERACTION_ROWS, ["对象", "来源", "规范", "说明"], [24 * mm, 38 * mm, 40 * mm, doc.width - 102 * mm]),
            Spacer(1, 12),
            Paragraph("页面模板", styles["h2"]),
            Paragraph("门户页强调服务入口和信息可读性；志愿者与管理员共用工作台骨架，通过角色色带进行区分；管理端列表页必须复用统一的筛选和分页骨架。", styles["body"]),
            make_template_showcase(doc.width),
            Spacer(1, 12),
            Paragraph("响应式规范", styles["h2"]),
            make_info_table(BREAKPOINT_ROWS, ["断点", "来源", "变化", "目标"], [20 * mm, 48 * mm, 40 * mm, doc.width - 108 * mm]),
            Spacer(1, 12),
            Paragraph("实现映射", styles["h2"]),
        ]
    )

    for path in SOURCE_FILES:
        story.append(Paragraph(f"- {path}", styles["body"]))

    story.extend(
        [
            Spacer(1, 10),
            Paragraph("执行要求", styles["h2"]),
            Paragraph("1. 新页面优先消费 token，不允许重新定义平行主题变量。", styles["body"]),
            Paragraph("2. Element Plus 视觉修改统一进入 element-overrides.css，不在页面内临时覆盖。", styles["body"]),
            Paragraph("3. 危险操作统一接入共享确认工具，反馈文案与状态提示保持一致。", styles["body"]),
            Paragraph("4. 门户、志愿者、管理员可以区分，但不允许脱离社区志愿服务管理这一业务核心。", styles["body"]),
        ]
    )

    doc.build(story, onFirstPage=add_page_decor, onLaterPages=add_page_decor)


def verify_pdf() -> None:
    if not PDF_PATH.exists():
        raise FileNotFoundError(f"PDF not generated: {PDF_PATH}")

    reader = PdfReader(str(PDF_PATH))
    if len(reader.pages) < 4:
        raise ValueError("UI manual PDF should contain at least 4 pages")

    with pdfplumber.open(str(PDF_PATH)) as pdf:
        extracted = "\n".join((page.extract_text() or "") for page in pdf.pages[:2])
        required_tokens = ["UI", "规范", "社区志愿服务平台", "设计原则"]
        missing = [token for token in required_tokens if token not in extracted]
        if missing:
            raise ValueError(f"PDF text verification failed, missing tokens: {missing}")

    if TMP_PREVIEW_DIR.exists():
        shutil.rmtree(TMP_PREVIEW_DIR)
    TMP_PREVIEW_DIR.mkdir(parents=True, exist_ok=True)

    document = fitz.open(str(PDF_PATH))
    rendered_paths = []
    for index, page in enumerate(document, start=1):
        pixmap = page.get_pixmap(matrix=fitz.Matrix(1.7, 1.7), alpha=False)
        output_path = TMP_PREVIEW_DIR / f"ui-manual-page-{index}.png"
        pixmap.save(output_path)
        rendered_paths.append(output_path)

    if not rendered_paths or any(path.stat().st_size == 0 for path in rendered_paths):
        raise ValueError("PDF preview rendering failed")

    shutil.rmtree(TMP_PREVIEW_DIR)


def main() -> None:
    FILE_DIR.mkdir(parents=True, exist_ok=True)
    MARKDOWN_PATH.write_text(build_markdown(), encoding="utf-8")
    build_pdf()
    verify_pdf()
    print(f"markdown: {MARKDOWN_PATH}")
    print(f"pdf: {PDF_PATH}")


if __name__ == "__main__":
    main()
