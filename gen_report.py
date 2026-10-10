# encoding: utf-8
"""生成葛仙米环境监测系统——实训总结报告 .docx"""
from docx import Document
from docx.shared import Pt, Cm, Inches, RGBColor, Emu
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
import os

doc = Document()

# ════════════════════ 页面设置 ════════════════════
for section in doc.sections:
    section.top_margin = Cm(2)
    section.bottom_margin = Cm(2)
    section.left_margin = Cm(2.5)
    section.right_margin = Cm(2)

# ════════════════════ 全局默认样式 ════════════════════
style = doc.styles['Normal']
pf = style.paragraph_format
pf.line_spacing = Pt(16)
pf.space_before = Pt(0)
pf.space_after = Pt(0)
font = style.font
font.name = '宋体'
font.size = Pt(12)
style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')

def set_cn_font(run, name='宋体', size=None, bold=False):
    run.font.name = name
    run._element.rPr.rFonts.set(qn('w:eastAsia'), name)
    if size: run.font.size = Pt(size)
    run.bold = bold

def p(text, bold=False, center=False, indent=True, size=12, font_name='宋体', italic=False):
    par = doc.add_paragraph()
    pf = par.paragraph_format
    pf.line_spacing = Pt(16)
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    if indent: pf.first_line_indent = Cm(0.74)
    else: pf.first_line_indent = Cm(0)
    if center: par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = par.add_run(text)
    set_cn_font(run, font_name, size, bold)
    if italic: run.italic = True
    return par

def heading(text, level=1):
    h = doc.add_heading(text, level=level)
    hf = h.runs[0].font if h.runs else None
    if hf:
        hf.name = '黑体'
        hf.element.rPr.rFonts.set(qn('w:eastAsia'), '黑体')
        hf.color.rgb = RGBColor(0, 0, 0)
        if level == 1: hf.size = Pt(14)
        elif level == 2: hf.size = Pt(13)
        else: hf.size = Pt(12)
    return h

def add_table(headers, rows):
    table = doc.add_table(rows=1+len(rows), cols=len(headers), style='Table Grid')
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, h in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = ''
        ph = cell.paragraphs[0]
        ph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = ph.add_run(h)
        set_cn_font(r, '宋体', 10, bold=True)
    for ri, row in enumerate(rows):
        for ci, val in enumerate(row):
            cell = table.rows[ri+1].cells[ci]
            cell.text = ''
            ph = cell.paragraphs[0]
            r = ph.add_run(str(val))
            set_cn_font(r, '宋体', 10)
    doc.add_paragraph()
    return table

def img_hint(text):
    """插图提示"""
    par = doc.add_paragraph()
    par.paragraph_format.first_line_indent = Cm(0)
    par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = par.add_run(f'【插入图片：{text}】')
    set_cn_font(run, '宋体', 10)
    run.font.color.rgb = RGBColor(128, 128, 128)
    run.italic = True

def tbl_caption(text):
    par = doc.add_paragraph()
    par.paragraph_format.first_line_indent = Cm(0)
    par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = par.add_run(text)
    set_cn_font(r, '黑体', 10, bold=True)

def page_break():
    doc.add_page_break()

# ════════════════════ 封面 ════════════════════
for _ in range(3):
    doc.add_paragraph()

p('宜春学院人工智能与信息工程学院实训', bold=True, center=True, size=16, indent=False)
doc.add_paragraph()
doc.add_paragraph()
p('基于Spring Boot的葛仙米生长环境', bold=True, center=True, size=22, indent=False, font_name='黑体')
p('智能监测系统的设计与实现', bold=True, center=True, size=22, indent=False, font_name='黑体')
for _ in range(4):
    doc.add_paragraph()
p('学    院：人工智能与信息工程学院', center=True, size=14, indent=False)
p('专    业：计算机科学与技术', center=True, size=14, indent=False)
p('姓    名：___________', center=True, size=14, indent=False)
p('学    号：___________', center=True, size=14, indent=False)
p('指导教师：___________', center=True, size=14, indent=False)
p('日    期：2026年7月', center=True, size=14, indent=False)
doc.add_paragraph()
p('宜春学院人工智能与信息工程学院教务处制', center=True, size=12, indent=False)

page_break()

# ════════════════════ 摘要 ════════════════════
heading('基于Spring Boot的葛仙米生长环境智能监测系统的设计与实现', 1)
doc.add_paragraph()

p('摘要：', bold=True, indent=False, size=12)
p('本文介绍了葛仙米生长环境智能监测系统的研究背景、项目意义和目前的研究与应用现状，明确了论文研究的内容和主要工作。在业务分析中，论文对系统存在的问题、组织机构、原有业务流程等进行了分析，给出了优化后的业务流程。在功能分析中，论文通过用例分析和用例描述，明确了系统的功能，给出了系统的总体功能包图和子功能包图，包括实时监测、历史数据、大棚管理、设备管理、告警记录、告警规则等子包图。在数据分析中，论文对系统各功能所需要处理的数据进行了分析，给出了实体类关系图，建立了数据库表结构。系统采用前后端分离的B/S架构，后端基于Java Spring Boot框架结合MyBatis-Plus持久层技术，前端采用Vue 3 + Element Plus + ECharts技术栈，通过RESTful API实现前后端交互。系统涵盖实时环境监测、历史数据分析、大棚与设备管理、告警监控与阈值配置、用户认证等功能模块，有助于提升葛仙米养殖的数字化管理水平。')
doc.add_paragraph()

p('关键词：', bold=True, indent=False, size=12)
p('葛仙米；环境监测系统；Spring Boot；Vue 3；需求分析；数据库建模', indent=False, size=12)
doc.add_paragraph()

p('Abstract：', bold=True, indent=False, size=12)
p('This thesis introduces the research background, project significance and current research and application status of the Gexianmi Growth Environment Intelligent Monitoring System, and identifies the main work and research contents. In business analysis, the thesis analyzes system problems, organizational structure, and original business processes, providing optimized business processes. In functional analysis, through use case analysis and description, the thesis clarifies system functions, presenting the overall functional package diagram and sub-package diagrams including real-time monitoring, historical data, greenhouse management, device management, alert records and alert rules. In data analysis, the thesis analyzes the data required by each function, presents entity relationship diagrams, and establishes database table structures. The system adopts a front-end and back-end separated B/S architecture, with a Java Spring Boot backend combined with MyBatis-Plus persistence layer and a Vue 3 + Element Plus + ECharts frontend, communicating via RESTful APIs. The system covers real-time environmental monitoring, historical data analysis, greenhouse and device management, alert monitoring and threshold configuration, and user authentication, helping to improve the digital management level of Gexianmi cultivation.')
doc.add_paragraph()

p('Keywords：', bold=True, indent=False, size=12)
p('Gexianmi; Environmental Monitoring System; Spring Boot; Vue 3; Requirement Analysis; Database Modelling', indent=False, size=12)

page_break()

# ════════════════════ 1 引言 ════════════════════
heading('1 引言', 1)

heading('1.1 编写目的', 2)
p('本文编写目的在于规定和控制项目开发的内容，保证本项目的需求分析活动在受控状态下进行，在进行软件开发前，明确本系统应达到的目标，对系统目标做出完整、准确、清晰、具体的要求。保证本需求分析的结果能够完整、无遗漏地反映待开发系统的要求。具体而言，本文旨在为"葛仙米生长环境智能监测系统"的开发提供完整的需求规格说明和设计方案，确保开发团队、测试人员和项目管理人员对系统目标达成共识。')

heading('1.2 读者对象', 2)
p('本文档的主要读者对象包括：')
p('（1）项目开发团队（前端工程师、后端工程师），通过本文档了解系统的功能需求和技术架构。')
p('（2）项目测试人员，通过本文档了解测试环境和测试计划。')
p('（3）项目管理人员，通过本文档跟踪项目进度和功能完成度。')
p('（4）指导教师，通过本文档评估项目的完整性和规范性。')

heading('1.3 术语解释', 2)
p('本小节提供正确理解此软件构架文档所需的全部术语的定义、首字母缩写词和缩略语。')

tbl_caption('表1-1 术语解释表')
add_table(
    ['术语/缩写', '全称', '说明'],
    [
        ['B/S', 'Browser/Server', '浏览器/服务器架构，用户通过浏览器访问部署在服务器上的应用程序'],
        ['RESTful API', 'Representational State Transfer API', '基于HTTP协议的轻量级Web服务接口风格'],
        ['JWT', 'JSON Web Token', '用于身份认证的加密令牌，包含用户身份信息和过期时间'],
        ['ORM', 'Object Relational Mapping', '对象关系映射，在Java对象与数据库表之间建立自动映射'],
        ['MyBatis-Plus', 'MyBatis-Plus', 'MyBatis的增强工具包，提供自动CRUD、分页、条件构造等功能'],
        ['ECharts', 'Enterprise Charts', '百度开源的JavaScript图表库，支持仪表盘和折线图等多种图表'],
        ['UUID', 'Universally Unique Identifier', '通用唯一标识符'],
        ['BCrypt', 'BCrypt', '一种基于Blowfish算法的密码哈希函数，用于密码安全存储'],
        ['CORS', 'Cross-Origin Resource Sharing', '跨域资源共享，允许浏览器向不同域名的服务器发送请求'],
        ['Vue 3', 'Vue.js 3.x', '渐进式JavaScript前端框架，采用Composition API语法'],
    ]
)

page_break()

# ════════════════════ 2 项目概述 ════════════════════
heading('2 项目概述', 1)

heading('2.1 项目背景', 2)
p('葛仙米是一种具有较高经济价值的水生食用藻类，其养殖过程对水温、pH值、溶解氧、光照强度、电导率等环境参数有着严格的要求[1]。传统养殖管理依赖人工定时测量和纸质记录，存在数据采集不及时、难以实现连续监测、数据分散无法有效追溯和分析、环境参数异常无法第一时间告警等问题。不同养殖大棚之间数据孤立，缺乏统一管理平台，管理者难以实时掌握全局状态。')
p('随着物联网传感器技术的发展和Web应用技术的成熟，构建一套低成本、易部署的环境监测系统已成为可能。利用传感器实时采集数据，通过互联网传输到服务器存储和分析，再以可视化大屏呈现在管理端，可以有效解决传统方式的痛点。本项目来源于实际葛仙米养殖场景需求，旨在设计并实现集数据采集、存储、展示、告警于一体的智能环境监测系统。')
p('本项目具有以下优势：一是通过Web端统一管理多棚区数据，消除信息孤岛；二是实时监测与自动告警相结合，及时发现异常；三是历史数据可查询可导出，为养殖决策提供数据支撑。')

heading('2.2 项目目标', 2)
p('本项目开发的意图和应用目标如下：')
p('（1）构建一套Web端环境监测大屏看板，支持管理员实时查看各养殖筒的水温、pH值、溶解氧、光照强度、电导率等5项环境参数。')
p('（2）实现历史数据的查询和可视化分析，支持CSV格式数据导出，为养殖管理提供数据支撑。')
p('（3）实现大棚和设备的统一管理，支持增删改查操作，自动统计各棚区设备数量。')
p('（4）建立告警管理机制，支持告警阈值的在线配置和告警记录的确认处理。')
p('（5）实现基于JWT的用户认证和权限控制，保障系统安全。')
p('（6）系统应具有良好的响应速度和用户体验，前后端分离架构便于后续扩展和维护。')

page_break()

# ════════════════════ 3 系统总体功能 ════════════════════
heading('3 系统总体功能', 1)
p('葛仙米生长环境智能监测系统主要面向葛仙米养殖基地的管理需求，提供环境数据采集、实时监测、历史分析、大棚管理、设备管理、告警监控和告警规则配置等核心功能。系统的总体功能结构如下：')

img_hint('图3-1 系统功能结构图。用层次结构图展示7个功能模块及其子功能：1.登录注册（登录/注册/修改密码）2.实时监测（仪表盘/迷你走势/设备表/告警条）3.历史数据（曲线图/快捷时间/CSV导出）4.大棚管理（列表/搜索/新增/编辑/删除）5.设备管理（列表/新增/编辑/删除）6.告警记录（列表/筛选/确认/忽略）7.告警规则（列表/编辑阈值/启用禁用）')

p('对上图3-1加以详细说明：系统分为七个核心功能模块。登录注册模块是整个系统的入口，所有其他模块依赖此模块进行用户身份验证。实时监测模块是操作员日常使用频率最高的功能，展示5参数仪表盘、1小时迷你走势、设备状态表和活跃告警。历史数据模块提供完整的时间范围查询和数据分析能力。大棚管理模块与设备管理模块通过外键关联，实现棚区与设备的关联管理。告警记录模块与告警规则模块协同工作，规则模块配置阈值，记录模块存储告警历史。')

page_break()

# ════════════════════ 4 业务需求分析 ════════════════════
heading('4 业务需求分析', 1)

heading('4.1 葛仙米环境监测业务', 2)

heading('4.1.1 业务需求描述', 3)
p('葛仙米养殖环境监测主要涉及以下业务：')
p('（1）环境数据采集业务。养殖大棚中安装有水温、pH、溶解氧、光照、电导率等传感器。目前采用人工定时测量方式，每隔数小时到各养殖筒手动测量并记录，每次约需15-20分钟。记录以纸质或Excel保存，数据分散难追溯。')
p('（2）数据监控业务。养殖人员需要实时了解各筒环境参数是否在正常范围内。目前靠人工目视检查仪表，发现问题后电话通知管理员。响应速度慢，夜间无人值守时可能数小时后才发现问题。')
p('（3）设备管理业务。每个大棚内有多个养殖筒，每筒对应一个传感器设备。设备信息记录在纸质台账中，新增、更换、维护缺乏系统化管理。')
p('（4）告警处理业务。环境参数超出正常范围时，需及时通知相关人员处置并记录告警信息。目前缺乏自动告警机制，完全依赖人工判断。')
p('（5）大棚管理业务。养殖基地分为多个大棚区域，各棚下有若干养殖筒。需对大棚基本信息进行管理维护。')

heading('4.1.2 业务流程', 3)
p('利用UML建模图中活动图来阐述当前的业务流向。原有业务流程中，环境数据监测的业务流程为：养殖技术人员定时到各养殖筒使用手持仪器测量参数→将数据记录在纸质表格或Excel中→下班前将当日数据汇总→交给管理员审核→管理员将数据整理为周报或月报→提交给基地负责人审阅。整个流程人工参与度高、效率低、数据滞后严重。')

img_hint('图4-1 原有环境监测业务活动图。用UML活动图展示从"人工测量"→"纸质记录"→"汇总报表"→"提交审核"的完整流程。划分泳道：养殖技术员/管理员/基地负责人')

p('引入本系统后的业务流程如图4-2所示，传感器定时采集数据自动上报至服务器，管理员通过Web大屏实时查看仪表盘和走势曲线，系统自动检测异常并生成告警，操作员即时确认处理。与原流程相比，实现了三大改进：数据采集从人工变为全自动、告警从被动发现变为主动推送、数据追溯从翻找纸质记录变为一键查询。')

img_hint('图4-2 优化后环境监测业务活动图。用UML活动图展示从"传感器采集"→"API上报"→"服务器存储"→"大屏展示"→"异常告警"→"操作员确认"的优化流程。划分泳道：传感器/系统服务器/操作员/管理员')

heading('4.2 大棚与设备管理业务', 2)
heading('4.2.1 业务需求描述', 3)
p('管理员需要对养殖基地的大棚信息和设备信息进行日常维护。新增养殖筒时需要注册设备并绑定到对应大棚。设备更换、维修时需要更新设备状态。目前这些信息分散在纸质台账和Excel表格中，查询和统计不便。')
heading('4.2.2 业务流程', 3)
p('优化后的业务流程为：管理员登录系统→进入大棚管理页，可新增/编辑/删除大棚信息→进入设备管理页，可新增设备并选择所属大棚→系统自动维护大棚与设备的关联关系→大棚列表自动统计各棚区设备数量。')

img_hint('图4-3 大棚设备管理业务活动图。展示管理员登录→选择大棚/设备管理→CRUD操作→关联维护→列表展示的流程')

heading('4.3 告警处理业务', 2)
heading('4.3.1 业务需求描述', 3)
p('当传感器数据超出设定的阈值范围时，系统需要自动生成告警记录并展示给操作员。操作员查看告警详情后，可确认告警（表示已知晓并正在处理）或忽略告警（表示误报或无需处理）。管理员可在告警规则配置页调整各参数的阈值。')
heading('4.3.2 业务流程', 3)
p('优化后的告警处理流程为：环境中数据上报→系统自动检查参数值是否在阈值范围内→若超出则生成告警记录→告警信息在实时监测页的告警滚动条中展示→操作员查看告警详情→操作员确认或忽略告警→告警状态更新。')

img_hint('图4-4 告警处理业务活动图。展示数据上报→阈值检查→告警生成→告警展示→操作员确认/忽略→告警状态更新的流程')

page_break()

# ════════════════════ 5 系统功能需求 ════════════════════
heading('5 系统功能需求', 1)
p('本章对获取的客户需求进行描述和分析，进行初步的抽象，形成概要和轮廓性的系统功能描述。')

heading('5.1 系统总用例图', 2)
p('利用UML中的用例图，画出系统中的总用例图，用例图不需要太详细。葛仙米生长环境智能监测系统的总用例图如图5-1所示，涵盖了系统的全部七个子系统和三类参与者。')

img_hint('图5-1 系统总用例图。图中央为系统边界大矩形框，左侧三个参与者人形图标（管理员/操作员/观察者），框内为7个用例包：登录注册/实时监测/历史数据/大棚管理/设备管理/告警记录/告警规则。管理员关联全部7个包，操作员关联除告警规则外的6个包，观察者关联5个查看类包。每个包内标注子用例（椭圆）')

heading('5.1.2 系统中角色分析', 3)
p('详细阐述每个角色在系统中的作用：')

tbl_caption('表5-1 系统角色职责分析表')
add_table(
    ['角色名称', '角色值', '职责描述', '涉及功能模块'],
    [
        ['管理员', 'admin', '系统最高权限。负责系统的日常运维、用户账号管理、大棚和设备的基础信息维护、告警规则配置等工作', '全部7个功能模块'],
        ['操作员', 'operator', '养殖技术人员。负责查看环境实时数据、确认和处理告警信息、查询历史数据进行分析', '实时监测、历史数据、告警记录、大棚查看、设备查看'],
        ['观察者', 'viewer', '基地管理人员。主要权限为查看数据，不参与数据的修改和设备的配置', '实时监测、历史数据、大棚查看、设备查看、告警查看'],
    ]
)

heading('5.1.3 功能描述', 3)
p('对总用例图中的用例进行说明：')
p('（1）登录注册用例：提供用户登录、注册和密码修改功能。用户通过用户名和密码登录，系统验证后签发JWT Token。新用户可通过注册功能创建账户，默认角色为操作员。')
p('（2）实时监测用例：用户选择大棚和设备后，系统展示5参数仪表盘、最近1小时迷你走势曲线、设备状态列表和活跃告警滚动条。系统每5秒自动轮询最新数据。光照仪表盘根据数据时间戳自动切换白天/黑夜模式。')
p('（3）历史数据用例：用户选定设备后，可通过快捷时间按钮或手动选择起止时间查询历史环境数据。系统以双ECharts曲线图展示，支持鼠标悬浮精确读数和CSV格式数据导出。')
p('（4）大棚管理用例：提供大棚的增删改查功能，表格展示编号/名称/位置/面积/状态/设备数/备注/创建时间，支持搜索和筛选。')
p('（5）设备管理用例：提供设备的增删改查功能，表格展示编号/名称/所属大棚名称/位置/在线状态/安装日期。')
p('（6）告警记录用例：提供告警的查询和处理功能，支持按状态和级别筛选，支持单条确认/忽略和批量确认操作。')
p('（7）告警规则配置用例：管理员可在线修改5个环境参数的正常范围和告警阈值，支持启用/禁用参数告警功能。')

heading('5.2 详细功能分析', 2)
p('根据总用例图中的各个子系统或模块进行分析，详细阐述每个用例如何完成及实现，通过用例描述进行阐述。')

# --- 5.2.1 登录 ---
heading('5.2.1 登录', 3)

tbl_caption('表5-2 登录用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '用户登录'],
        ['用例编号', 'UC-001'],
        ['参与者', '管理员、操作员、观察者'],
        ['前置条件', '用户已注册，拥有有效的用户名和密码，数据库中存在该用户的记录'],
        ['基本事件流', '1.用户访问系统URL，系统显示登录页面\n2.用户输入用户名和密码\n3.用户点击"登录"按钮或按回车键\n4.系统发送POST请求至/api/auth/login\n5.后端AuthController接收请求\n6.AuthServiceImpl查询users表验证用户\n7.BCrypt.verify()验证密码\n8.系统调用JwtUtil.generate()签发JWT Token（有效期24小时）\n9.系统返回{code:200,data:{token,userInfo}}\n10.前端将token存入localStorage\n11.前端跳转至实时监测页/monitor'],
        ['可选事件流', '用户可勾选"记住密码"复选框，前端将凭证加密存入localStorage，7天内自动填充'],
        ['异常事件流', '4a.用户名为空或密码为空，前端表单校验提示"请输入用户名/密码"\n5a.用户名不存在，后端返回code:401提示"用户名或密码错误"\n5b.密码不匹配，后端返回code:401提示"用户名或密码错误"\n5c.Token过期后，用户访问其他API返回401，前端清除Token并跳回登录页'],
        ['后置条件', 'JWT Token存入浏览器localStorage。后续所有API请求自动通过axios拦截器注入Authorization: Bearer {token}请求头。用户信息存入localStorage供App.vue显示'],
        ['优先级', '高（1级）'],
    ]
)

# --- 5.2.2 注册 ---
heading('5.2.2 注册', 3)

tbl_caption('表5-3 注册用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '用户注册'],
        ['用例编号', 'UC-002'],
        ['参与者', '未注册用户（管理员、操作员、观察者）'],
        ['前置条件', '用户访问登录页，点击"立即注册"切换到注册模式'],
        ['基本事件流', '1.用户在登录页点击"立即注册"切换为注册模式\n2.用户输入用户名、密码和确认密码\n3.用户点击"注册"按钮\n4.前端表单校验：两次密码一致性、密码长度≥6位\n5.前端发送POST请求至/api/auth/register\n6.AuthServiceImpl.register()检查用户名唯一性\n7.系统使用BCrypt加密密码生成password_hash\n8.系统设置默认角色为operator\n9.系统将用户记录插入users表\n10.系统返回{code:200,msg:"注册成功"}'],
        ['异常事件流', '4a.两次密码不一致，前端提示"两次密码不一致"\n4b.密码不足6位，前端提示"密码至少6位"\n5a.用户名已存在，后端返回code:400提示"用户名已存在"'],
        ['后置条件', '用户记录写入users表，默认角色为operator。2秒后自动切换回登录模式'],
        ['优先级', '中（2级）'],
    ]
)

# --- 5.2.3 实时监测 ---
heading('5.2.3 实时监测', 3)

tbl_caption('表5-4 实时监测用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '实时环境数据监测'],
        ['用例编号', 'UC-003'],
        ['参与者', '管理员、操作员、观察者'],
        ['前置条件', '用户已登录，系统中存在大棚和设备的注册信息'],
        ['基本事件流', '1.系统默认选择第一个大棚和第一个设备\n2.前端调用fetchLatest(deviceId)获取最新数据\n3.前端调用fetchRecent(deviceId,60)获取最近60条数据\n4.前端调用fetchActiveAlerts(deviceId)获取活跃告警\n5.GaugePanel组件渲染5个ECharts仪表盘\n6.系统渲染1小时迷你走势曲线（5参数上下排列）\n7.系统渲染设备状态表格\n8.系统渲染活跃告警滚动条\n9.系统每5秒重复步骤2和4'],
        ['可选事件流', '用户可通过大棚和设备下拉框切换查看目标\n用户可点击"查看完整历史曲线"按钮跳转至/history页面'],
        ['异常事件流', '2a.设备无数据，仪表盘显示"等待数据"\n2b.API请求超时，保持上次数据不变\n5a.光照仪数据为夜间值（<100 lux），仪表盘自动切换为夜间模式（范围0-200 lux）'],
        ['后置条件', '仪表盘持续以5秒间隔刷新，页面不可见（visibilitychange）时暂停轮询'],
        ['优先级', '高（1级）'],
    ]
)

# --- 5.2.4 历史数据 ---
heading('5.2.4 历史数据查询', 3)

tbl_caption('表5-5 历史数据查询用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '历史环境数据查询'],
        ['用例编号', 'UC-004'],
        ['参与者', '管理员、操作员、观察者'],
        ['前置条件', '用户已登录，已选择大棚和设备'],
        ['基本事件流', '1.用户选择设备后，系统自动加载该设备的数据范围信息\n2.页面显示"该设备记录了{开始日期}至{结束日期}的数据，共{count}条记录"\n3.用户点击快捷时间按钮（1h/6h/24h/3d/7d）\n4.系统自动计算起止时间，调用fetchHistory(deviceId,start,end)\n5.系统在双ECharts曲线图中渲染数据（上图：水温+溶解氧，下图：pH值+光照）\n6.用户可鼠标悬浮查看具体数值\n7.用户可点击"导出CSV"按钮将数据导出为UTF-8 BOM编码的CSV文件'],
        ['可选事件流', '用户可手动选择起止时间后再查询\n用户可切换设备查看不同设备的数据'],
        ['异常事件流', '3a.所选时间段内无数据，系统提示相应警告信息\n4a.API请求超时或失败，显示"加载失败"提示'],
        ['后置条件', '曲线图展示全部查询结果，用户可导出CSV文件用于Excel分析'],
        ['优先级', '高（1级）'],
    ]
)

# --- 5.2.5 大棚管理 ---
heading('5.2.5 大棚管理', 3)

tbl_caption('表5-6 大棚管理用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '大棚信息管理'],
        ['用例编号', 'UC-005'],
        ['参与者', '管理员'],
        ['前置条件', '用户已登录且角色为admin'],
        ['基本事件流', '1.管理员进入大棚管理页面\n2.系统加载所有大棚列表，后端自动填充每棚设备数\n3.管理员可通过搜索框按编号/名称过滤\n4.管理员点击"新增大棚"按钮\n5.系统弹出对话框，管理员填写编号/名称/位置/面积/备注\n6.管理员提交表单，系统验证并写入greenhouses表\n7.系统提示"大棚已创建"并刷新列表\n8.管理员点击某行"编辑"按钮\n9.系统弹出预填充的编辑对话框\n10.管理员修改信息后提交，系统更新记录\n11.系统提示"大棚信息已更新"\n12.管理员点击某行"删除"按钮\n13.系统检查该棚下设备数是否为0\n14.若为0则删除并提示成功，若不为0则提示"请先移除设备"'],
        ['后置条件', 'greenhouses表记录被更新，列表同步刷新'],
        ['优先级', '高（1级）'],
    ]
)

# --- 5.2.6 设备管理 ---
heading('5.2.6 设备管理', 3)

tbl_caption('表5-7 设备管理用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '设备信息管理'],
        ['用例编号', 'UC-006'],
        ['参与者', '管理员'],
        ['前置条件', '用户已登录且角色为admin，系统中存在大棚记录'],
        ['基本事件流', '1.管理员进入设备管理页面\n2.系统加载设备列表，后端自动查询并填充所属大棚名称\n3.管理员点击"新增设备"按钮\n4.系统弹出对话框，管理员填写编号/名称/所属大棚/安装位置/备注\n5.管理员选择所属大棚（下拉框从greenhouses表加载）\n6.管理员提交表单，系统验证并写入devices表\n7.系统提示"已创建"并刷新列表\n8.管理员可编辑和删除设备记录'],
        ['后置条件', 'devices表记录被更新，列表同步刷新'],
        ['优先级', '高（1级）'],
    ]
)

# --- 5.2.7 告警记录 ---
heading('5.2.7 告警记录', 3)

tbl_caption('表5-8 告警记录用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '告警记录管理'],
        ['用例编号', 'UC-007'],
        ['参与者', '管理员、操作员'],
        ['前置条件', '用户已登录，系统中存在告警记录'],
        ['基本事件流', '1.用户进入告警记录页面\n2.系统以表格展示告警记录（设备/参数/触发值/阈值/级别/状态/时间）\n3.用户可按状态（active/confirmed/ignored）筛选\n4.用户点击某条告警的"确认"按钮\n5.系统调用POST /api/alerts/{id}/confirm更新状态为confirmed\n6.用户点击某条告警的"忽略"按钮\n7.系统调用POST /api/alerts/{id}/ignore更新状态为ignored\n8.用户可点击"确认全部"一键确认所有活跃告警'],
        ['后置条件', 'alerts表状态字段被更新，列表同步刷新'],
        ['优先级', '中（2级）'],
    ]
)

# --- 5.2.8 告警规则 ---
heading('5.2.8 告警规则配置', 3)

tbl_caption('表5-9 告警规则配置用例描述')
add_table(
    ['描述项', '描述内容'],
    [
        ['用例名称', '告警规则配置'],
        ['用例编号', 'UC-008'],
        ['参与者', '管理员'],
        ['前置条件', '用户已登录且角色为admin'],
        ['基本事件流', '1.管理员进入告警规则页面\n2.系统从alert_rules表加载5条规则并展示\n3.表格显示参数名/单位/正常范围/告警阈值/级别/启用状态\n4.管理员点击某参数的"编辑"按钮\n5.系统弹出编辑对话框\n6.管理员修改正常范围/告警阈值/告警级别/启用开关\n7.管理员点击"保存"\n8.系统调用PUT /api/alert-rules/{id}更新规则\n9.系统提示"规则已更新"并刷新列表'],
        ['后置条件', 'alert_rules表被更新，仪表盘颜色阈值和告警判断依据新规则'],
        ['优先级', '中（2级）'],
    ]
)

page_break()

# ════════════════════ 6 系统需求优先级 ════════════════════
heading('6 系统需求优先级', 1)
p('列表描述所有功能的开发优先级并为它建立需求编号。优先级分为3级，按照开发的紧急和重要程度来划分：紧急且重要为1级；紧急但不重要或重要但不紧急为2级；不紧急且不重要为3级。')

tbl_caption('表6-1 系统需求优先级表')
add_table(
    ['需求编号', '功能模块', '功能名称', '优先级', '说明'],
    [
        ['REQ-001', '登录注册', '用户登录', '1级', '系统入口，所有功能依赖认证'],
        ['REQ-002', '登录注册', '用户注册', '2级', '方便新增用户，管理员账号预置'],
        ['REQ-003', '实时监测', '5参数仪表盘', '1级', '核心展示功能'],
        ['REQ-004', '实时监测', '1小时迷你走势', '2级', '辅助趋势查看'],
        ['REQ-005', '实时监测', '5秒自动轮询', '1级', '实时性保障'],
        ['REQ-006', '历史数据', '时间范围查询', '1级', '核心分析功能'],
        ['REQ-007', '历史数据', 'CSV数据导出', '2级', '数据分析辅助功能'],
        ['REQ-008', '大棚管理', '增删改查', '1级', '基础设施管理'],
        ['REQ-009', '设备管理', '增删改查', '1级', '基础设施管理'],
        ['REQ-010', '告警记录', '确认/忽略', '1级', '异常处理核心流程'],
        ['REQ-011', '告警规则', '阈值配置', '2级', '灵活调整告警标准'],
        ['REQ-012', '认证安全', 'JWT拦截', '1级', '安全保障'],
        ['REQ-013', '认证安全', '路由守卫', '2级', '前端安全防护'],
        ['REQ-014', '数据接口', '环境数据上报API', '1级', '传感器数据入口'],
    ]
)

page_break()

# ════════════════════ 7 非功能需求 ════════════════════
heading('7 非功能需求', 1)

heading('7.1 外部接口需求', 2)
p('（1）用户接口：系统提供基于Web浏览器的图形化用户界面，采用Vue 3 + Element Plus组件库构建。支持Chrome、Edge、Firefox等主流浏览器。界面要求简洁直观，大屏数据显示清晰可读。')
p('（2）硬件接口：系统通过HTTP接口接收传感器设备上报的环境数据。传感器设备需支持HTTP协议，以JSON格式POST数据至/api/environment/data接口。')
p('（3）软件接口：系统后端提供RESTful API接口，前端通过Axios调用。API接口遵循统一响应格式{code, data, msg}。接口认证采用JWT Bearer Token机制。')
p('（4）通信接口：系统采用HTTP/HTTPS协议进行通信。前端默认通过localhost:5173访问，后端默认监听8000端口。前后端分离部署时需配置CORS跨域白名单。')

heading('7.2 法规政策约束', 2)
p('本系统为葛仙米养殖基地内部使用的环境监测工具，不涉及个人隐私数据的收集和处理，暂无特殊法规政策约束。系统遵循基本的网络安全规范，用户密码采用BCrypt加密存储。')

heading('7.3 性能需求', 2)
p('（1）响应速度：实时监测接口（/api/environment/latest）的响应时间应在1秒以内。历史数据查询接口（/api/environment/history）在查询1000条以内数据时响应时间应在3秒以内。')
p('（2）可靠性：系统应保证7×24小时稳定运行。数据库应定期备份。')
p('（3）吞吐量：系统应支持至少5个传感器设备同时上报数据。')
p('（4）并发用户：系统应支持至少10个用户同时在线查看。')

heading('7.4 安全需求', 2)
p('（1）认证安全：所有API请求（除登录和注册外）均需携带有效的JWT Token。Token有效期为24小时，过期后需重新登录。')
p('（2）密码安全：用户密码采用BCrypt算法加密存储，不可逆。密码长度至少6位。')
p('（3）跨域安全：开发阶段CORS配置为允许所有来源。生产环境需严格限定允许的域名。')
p('（4）SQL注入防护：系统使用MyBatis-Plus的参数化查询，自动防止SQL注入攻击。')
p('（5）前端安全：路由守卫拦截未登录用户访问业务页面。响应拦截器自动处理401未授权响应。')

heading('7.5 系统运行需求', 2)
heading('7.5.1 软件需求', 3)
p('（1）后端运行环境：JDK 17 LTS或以上版本。Maven 3.9+用于项目构建。MySQL 8.0作为数据库。')
p('（2）前端运行环境：Node.js 20.19.0或以上版本。npm 11.9+用于依赖管理。现代浏览器（Chrome 90+、Edge 90+、Firefox 90+）。')
p('（3）操作系统：支持Windows 10/11、Linux、macOS。')

heading('7.5.2 硬件需求', 3)
p('（1）服务器：CPU 4核以上，内存8GB以上，硬盘100GB以上。推荐配置用于运行MySQL和Spring Boot应用。')
p('（2）客户端：能够运行现代浏览器的普通PC或笔记本即可，无特殊硬件要求。')
p('（3）传感器设备：支持HTTP通信的水温、pH、溶解氧、光照、电导率传感器。')

page_break()

# ════════════════════ 8 系统总体设计 ════════════════════
heading('8 系统总体设计', 1)

heading('8.1 体系结构设计', 2)
p('目前的系统设计中，主要采用的体系结构有B/S结构、C/S结构以及B/S和C/S混合结构。')
p('B/S结构，即浏览器(Browser)/服务器(Server)结构。浏览器是指客户端的应用程序需要在浏览器上运行。服务器是指开发好的应用程序部署在服务器上运行，客户端通过浏览器输入指定的服务器IP地址即可使用。B/S结构的特点包括：客户端免安装（应用程序只需部署在服务器即可，降低安装维护成本）、更新快（只需更新服务端应用程序，客户端即可使用更新后的版本）、容易集中控制、安全风险较大（需要通过防火墙等安全策略保护）、响应速度较慢（需要依靠硬件提升）。')
p('C/S结构，即客户端(Client)和服务器(Server)结构。C/S结构分为客户机和服务器两层，客户机具有一定的数据处理和存储能力。特点包括：响应数据快（客户端有一定的处理能力，可先处理数据再发给服务器）、安全性较好（客户端和服务器同属一个系统，通讯封闭）、维护成本高（每个客户端都需安装维护）、数据一致性控制较难。')
p('根据本项目的实际需求，养殖基地管理者希望系统维护简单方便，不需要在客户端安装专门软件即可通过浏览器访问。因此决定系统体系结构采用B/S架构。系统总体架构如图8-1所示。')

img_hint('图8-1 葛仙米环境监测系统体系结构图。采用四层B/S架构图：自上而下为"浏览器端（Vue3+ElementPlus+ECharts）→HTTP/JSON→Spring Boot服务器（Controller控制层→Service业务层→Mapper数据层）→JDBC→MySQL数据库"。左侧标注"客户端"，右侧标注"服务器端"，中间用防火墙图标分隔。底部标注部署环境：Windows Server 2019 / Linux')

heading('8.2 系统功能架构设计', 2)

img_hint('图8-2 系统功能架构设计图。用层次结构图展示系统7大功能模块的层级关系。顶层为"葛仙米环境智能监测系统"，第二层为7个模块名，第三层为各模块的子功能列表')

p('下面对每一个子系统的功能进行说明：')
p('登录注册子系统：包含登录、注册、修改密码三个功能项。登录功能通过JWT实现无状态认证，注册功能支持创建默认角色为操作员的新账户。')
p('实时监测子系统：包含仪表盘展示、1小时迷你走势、设备状态表和活跃告警条。核心是5个ECharts gauge仪表盘，以绿/橙/红三段弧线展示参数状态。')
p('历史数据子系统：包含双曲线图展示、快捷时间选择、手动时间选择、CSV数据导出。曲线图分上下两图各显示2个参数，避免标签重叠。')
p('大棚管理子系统：包含列表查询（含搜索过滤）、新增、编辑、删除。列表自动统计各棚下设备数量。')
p('设备管理子系统：包含列表查询（含所属大棚名称）、新增、编辑、删除。新增时需选择所属大棚。')
p('告警记录子系统：包含状态筛选、告警确认、告警忽略、批量确认。')
p('告警规则子系统：包含规则列表展示和规则编辑。修改后实时生效。')

heading('8.2.1 实时监测子系统功能构建设计', 3)

img_hint('图8-3 实时监测子系统功能结构图。展示该子系统的5个子功能：大棚设备选择/5参数仪表盘/1h迷你走势曲线/设备状态列表/活跃告警滚动条')

p('实时监测子系统是本系统使用频率最高的功能模块，为操作员提供即时的环境状态视图。大棚与设备选择采用二级下拉框联动，选择后自动加载该设备的所有数据。仪表盘根据实际数据波动范围进行了优化（如水温范围19-26°C，绿区占71%弧线），指针通常位于绿色区域中间。1小时迷你走势用5条小型ECharts折线图上下排列，展示最近60条数据的趋势，只读不交互。设备状态表以el-table形式列出当前大棚下所有设备及其在线状态。活跃告警条以列表形式展示当前未处理的告警信息。')

heading('8.2.2 历史数据子系统功能构建设计', 3)

img_hint('图8-4 历史数据子系统功能结构图。展示该子系统的4个子功能：设备选择/时间范围查询（快捷按钮+手动选择）/双曲线图展示/CSV导出')

p('历史数据子系统为用户提供详细的数据分析能力。与实时监测不同，该子系统专注于对已采集数据的深入分析。快捷时间按钮（1h/6h/24h/3d/7d）使用户一键切换时间范围。双曲线图采用上下两个独立ECharts实例，每个实例仅2条Y轴，避免标签重叠。CSV导出功能将查询结果以UTF-8 BOM编码保存，Excel可直接打开，文件名自动包含设备编号和日期。')

heading('8.3 功能设计', 2)
p('将系统分解为若干子系统，绘制功能模块结构图，说明整个系统及各子系统之间的关系和各自的主要功能。本系统按"高内聚、低耦合"原则进行模块划分：前端各Vue组件通过Props和Events通信，与后端通过RESTful API解耦。后端各Controller独立负责各自的业务域，共享通用组件（Result统一响应、JwtInterceptor认证、GlobalExceptionHandler异常处理）。')

heading('8.3.1 实时监测功能', 3)
p('（一）功能结构设计', bold=True, indent=False)
p('实时监测功能是系统的核心展示功能。用户通过大棚和设备下拉框选择目标设备后，系统在同一个页面展示：5参数仪表盘（水温、pH值、溶解氧、光照、电导率）、1小时迷你走势曲线、设备状态列表和活跃告警信息。光照仪表盘根据数据时间戳中的小时数自动切换白天模式（6:00-18:00，范围0-2000 lux）和黑夜模式（18:00-次日6:00，范围0-200 lux）。')

img_hint('图8-5 实时监测功能结构图。展示页面从上到下的布局：大棚设备选择栏→5个仪表盘（5列）→1h迷你走势曲线→设备状态表+告警滚动条（两列）')

p('（二）功能类设计', bold=True, indent=False)

img_hint('图8-6 实时监测功能类图。展示RealTimeMonitor.vue/GaugePanel.vue/EnvironmentController/EnvironmentService/EnvironmentServiceImpl/EnvironmentRecord等类及其关系。标注Vue组件与Controller之间的HTTP调用关系')

tbl_caption('表8-1 EnvironmentController类描述')
add_table(
    ['属性/方法', '类型', '说明'],
    [
        ['environmentService', 'EnvironmentService', '环境数据业务服务（依赖注入）'],
        ['receiveData()', 'POST /api/environment/data', '接收传感器上报的环境数据，参数校验后写入数据库'],
        ['getLatest()', 'GET /api/environment/latest', '查询指定设备最新一条环境数据'],
        ['getRecent()', 'GET /api/environment/recent', '查询指定设备最近N条数据，用于迷你走势'],
        ['getHistory()', 'GET /api/environment/history', '按设备ID和时间范围查询历史数据'],
        ['getDataRange()', 'GET /api/environment/range', '查询指定设备的起止时间和总记录数'],
        ['listDevices()', 'GET /api/environment/devices', '获取所有设备列表'],
    ]
)

tbl_caption('表8-2 GaugePanel组件描述')
add_table(
    ['属性/方法', '类型', '说明'],
    [
        ['data', 'Props: EnvRecord|null', '父组件传入的最新环境数据，包含5个参数值和时间戳'],
        ['gaugeDefs', 'Array<GaugeDef>', '仪表盘定义数组，含每个参数的min/max/normal/warn范围'],
        ['lightDef()', 'Function', '根据时间戳判断白天/黑夜，返回光照仪表盘的动态范围'],
        ['initAllCharts()', 'Function', '初始化5个ECharts gauge实例并绑定resize事件'],
        ['getAxisColor()', 'Function', '计算仪表盘弧线的红-橙-绿-橙-红五段颜色分布'],
        ['statusColor()', 'Function', '根据当前值和阈值返回底部状态圆点的颜色（绿/橙/红）'],
    ]
)

p('（三）界面设计', bold=True, indent=False)
p('实时监测页面采用垂直布局，从上到下分为四个区域：')
p('第一区域（设备选择栏）：横向排列大棚下拉框、设备下拉框、状态标签和"完整历史曲线"跳转按钮。背景为白色圆角卡片。')
p('第二区域（仪表盘区）：5个ECharts仪表盘横向均匀排列，各自独立容器。每盘顶部显示参数名，下部显示颜色状态圆点和数值+单位。')
p('第三区域（迷你走势区）：el-card卡片包裹，5条小型ECharts折线图垂直排列。每条仅保留坐标轴和数据线，无图例和工具栏。')
p('第四区域（信息区）：el-row布局，左16列放设备状态el-table，右8列放告警信息列表。')

img_hint('图8-7 实时监测页面界面设计图。展示页面的完整布局效果（可截取实际运行截图）')

p('（四）时序图设计', bold=True, indent=False)
p('时序图用于展示功能模块如何按照时间先后次序完成，确定功能完成时所需的对象、对象参与的先后次序，指导该功能编码实现。实时监测功能的数据加载时序图如图8-8所示。')

img_hint('图8-8 实时监测数据加载时序图。从上到下按时间线排列对象：用户/RealTimeMonitor.vue/GaugePanel.vue/EnvironmentController/EnvironmentService/EnvironmentRecordMapper/MySQL。用箭头标注各步骤：1.用户选择设备→2.fetchLatest()→3.GET /api/environment/latest→4.selectOne()→5.SELECT→6.返回数据→7.更新仪表盘。标注每5秒重复步骤2-7')

heading('8.3.2 大棚管理功能', 3)
p('（一）功能结构设计', bold=True, indent=False)
p('大棚管理功能提供大棚基本信息的增删改查。表格展示所有大棚，支持按编号和名称搜索。新增和编辑通过对话框实现，保存后列表自动刷新。删除时检查该棚下设备数，不为0时阻止删除。')

img_hint('图8-9 大棚管理功能结构图。展示大棚管理页面的5个子功能：搜索过滤/列表展示/新增大棚/编辑大棚/删除大棚')

p('（二）功能类设计', bold=True, indent=False)

img_hint('图8-10 大棚管理功能类图。展示GreenhouseManage.vue/GreenhouseController/GreenhouseService/GreenhouseServiceImpl/GreenhouseMapper/Greenhouse实体等类的关系')

tbl_caption('表8-3 GreenhouseController类描述')
add_table(
    ['属性/方法', '类型', '说明'],
    [
        ['listAll()', 'GET /api/greenhouses', '查询所有大棚，Service层自动填充每个大棚的设备数'],
        ['create()', 'POST /api/greenhouses', '新建大棚，接收编号/名称/位置/面积/备注'],
        ['update()', 'PUT /api/greenhouses/{id}', '编辑大棚信息，仅允许修改名称/位置/面积/备注'],
        ['delete()', 'DELETE /api/greenhouses/{id}', '删除大棚，MyBatis-Plus逻辑删除'],
        ['getDevices()', 'GET /api/greenhouses/{id}/devices', '查询指定大棚下的所有设备列表'],
    ]
)

p('（三）界面设计', bold=True, indent=False)
p('大棚管理页面采用单表格+搜索+对话框的经典CRUD布局。顶部为搜索框和新增按钮，中部为数据表格，底部为统计行。对话框包含编号/名称/位置/面积/备注5个表单项。面积使用el-input-number组件，备注使用textarea并显示字数统计。')

p('（四）时序图设计', bold=True, indent=False)

img_hint('图8-11 大棚新增时序图。从上到下排列：管理员/GreenhouseManage.vue/GreenhouseController/GreenhouseServiceImpl/GreenhouseMapper/MySQL。展示：1.点击新增→2.填写表单→3.POST /api/greenhouses→4.insert→5.INSERT SQL→6.返回成功→7.刷新列表→8.显示成功提示')

heading('8.3.3 告警规则配置功能', 3)
p('（一）功能结构设计', bold=True, indent=False)
p('告警规则配置功能允许管理员在线修改5个环境参数的告警阈值。5条规则以表格形式展示，每行显示参数名、单位、正常范围、告警阈值、告警级别和启用状态。点击编辑按钮弹出对话框，可修改正常范围上下限、告警阈值上下限、告警级别（警告/严重）和启用开关。修改保存后立即生效，仪表盘的颜色阈值同步更新。')

p('（二）功能类设计', bold=True, indent=False)

img_hint('图8-12 告警规则功能类图。展示AlertRules.vue/AlertRuleController/AlertRuleService/AlertRuleServiceImpl/AlertRuleMapper/AlertRule实体的关系')

p('（三）界面设计', bold=True, indent=False)
p('告警规则配置页面采用单表格+编辑对话框的布局。表格中正常范围用绿色字体显示，告警阈值用红色字体显示。启用状态用✅/❌ emoji直观展示。编辑对话框使用el-input-number精确设置阈值，使用el-radio-group选择告警级别，使用el-switch控制启用/禁用。')

p('（四）时序图设计', bold=True, indent=False)

img_hint('图8-13 告警规则编辑时序图。展示从管理员点击编辑→弹出对话框→修改阈值→保存→PUT /api/alert-rules/{id}→UPDATE SQL→返回成功→刷新列表的完整时序')

page_break()

# ════════════════════ 8.4 数据库设计 ════════════════════
heading('8.4 数据库设计', 2)

heading('8.4.1 逻辑设计', 3)
p('（一）实体关系设计', bold=True, indent=False)
p('系统涉及的实体包括：用户（User）、大棚（Greenhouse）、设备（Device）、环境数据记录（EnvironmentRecord）、告警记录（Alert）和告警规则（AlertRule）。各实体之间的关系如下：一个大棚包含多个设备（1:N）；一个设备产生多条环境数据记录（1:N）；一个设备可能触发多条告警记录（1:N）；告警规则为独立实体，不与其他实体有外键关联。')

img_hint('图8-14 系统实体关系图（E-R图）。用矩形表示6个实体（User/Greenhouse/Device/EnvironmentRecord/Alert/AlertRule）。用菱形标注关系：Greenhouse(1)---包含---(N)Device，Device(1)---产生---(N)EnvironmentRecord，Device(1)---触发---(N)Alert。AlertRule为独立实体无外键关联。每个实体旁标注主要属性（椭圆）')

p('（二）实体属性设计', bold=True, indent=False)

img_hint('图8-15 核心实体属性图。选取3个核心实体（Device/EnvironmentRecord/AlertRule）绘制属性图。Device实体标注code/name/greenhouse_id/location/status/installed_at/notes共7个属性。EnvironmentRecord实体标注id/device_id/recorded_at/water_temp/ph/dissolved_oxygen/light_intensity/conductivity/created_at共9个属性。AlertRule实体标注id/param/param_name/unit/min_val/max_val/threshold_low/threshold_high/level/enabled共10个属性')

p('（三）逻辑表格设计', bold=True, indent=False)

p('系统共设计6张数据表，分别为users用户表、greenhouses大棚表、devices设备表、environment_records环境数据记录表、alerts告警记录表和alert_rules告警规则配置表。各表结构设计如下。')

tbl_caption('表8-4 users 用户表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['id', 'BIGINT', '否', '主键', 'AUTO_INCREMENT', '用户ID'],
        ['username', 'VARCHAR(32)', '否', 'UNIQUE', '—', '登录用户名'],
        ['password_hash', 'VARCHAR(128)', '否', '—', '—', 'BCrypt加密后的密码'],
        ['role', 'VARCHAR(16)', '否', '—', 'operator', 'admin/operator/viewer'],
        ['last_login_at', 'DATETIME', '是', '—', 'NULL', '最后登录时间'],
        ['created_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '创建时间'],
    ]
)

tbl_caption('表8-5 greenhouses 大棚表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['id', 'BIGINT', '否', '主键', 'AUTO_INCREMENT', '大棚ID'],
        ['code', 'VARCHAR(32)', '否', 'UNIQUE', '—', '大棚编号'],
        ['name', 'VARCHAR(64)', '否', '—', '—', '大棚名称'],
        ['location', 'VARCHAR(128)', '是', '—', 'NULL', '所在位置'],
        ['status', "ENUM('normal','maintenance','closed')", '否', '—', 'normal', '运行状态'],
        ['area', 'DECIMAL(8,2)', '是', '—', 'NULL', '面积(m²)'],
        ['notes', 'TEXT', '是', '—', 'NULL', '备注'],
        ['is_deleted', 'TINYINT(1)', '否', '—', '0', '逻辑删除标识(MyBatis-Plus @TableLogic)'],
        ['created_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '创建时间'],
        ['updated_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '更新时间(MetaObjectHandler自动填充)'],
    ]
)

tbl_caption('表8-6 devices 设备表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['code', 'VARCHAR(32)', '否', '主键', '—', '设备唯一编号'],
        ['name', 'VARCHAR(64)', '否', '—', '—', '设备名称'],
        ['greenhouse_id', 'BIGINT', '是', '外键', 'NULL', '所属大棚ID(FK→greenhouses.id)'],
        ['location', 'VARCHAR(128)', '是', '—', 'NULL', '安装位置'],
        ['status', "ENUM('online','offline','maintenance')", '否', '—', 'offline', '在线状态'],
        ['installed_at', 'DATE', '是', '—', 'NULL', '安装日期'],
        ['notes', 'TEXT', '是', '—', 'NULL', '备注'],
        ['is_deleted', 'TINYINT(1)', '否', '—', '0', '逻辑删除标识'],
        ['created_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '创建时间'],
        ['updated_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '更新时间'],
    ]
)

tbl_caption('表8-7 environment_records 环境数据记录表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['id', 'BIGINT', '否', '主键', 'AUTO_INCREMENT', '记录ID'],
        ['device_id', 'VARCHAR(32)', '否', '外键', '—', '设备编号(FK→devices.code)'],
        ['recorded_at', 'DATETIME(3)', '否', '索引', '—', '传感器采集时间(前端字段名timestamp)'],
        ['water_temp', 'DECIMAL(5,2)', '是', '—', 'NULL', '水温(°C)'],
        ['ph', 'DECIMAL(4,2)', '是', '—', 'NULL', 'pH值'],
        ['dissolved_oxygen', 'DECIMAL(5,2)', '是', '—', 'NULL', '溶解氧(mg/L)'],
        ['light_intensity', 'DECIMAL(8,2)', '是', '—', 'NULL', '光照强度(lux)'],
        ['conductivity', 'DECIMAL(6,2)', '是', '—', 'NULL', '电导率(μS/cm)'],
        ['created_at', 'DATETIME(3)', '是', '—', 'CURRENT_TIMESTAMP(3)', '入库时间'],
    ]
)
p('索引：INDEX idx_device_time (device_id, recorded_at) — 用于加速按设备和时间范围查询。', indent=False)

tbl_caption('表8-8 alerts 告警记录表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['id', 'BIGINT', '否', '主键', 'AUTO_INCREMENT', '告警ID'],
        ['device_id', 'VARCHAR(32)', '否', '外键', '—', '触发设备编号(FK→devices.code)'],
        ['greenhouse_id', 'BIGINT', '是', '外键', 'NULL', '所属大棚ID(FK→greenhouses.id)'],
        ['param', 'VARCHAR(32)', '否', '—', '—', '告警参数名(waterTemp/ph等)'],
        ['param_name', 'VARCHAR(32)', '否', '—', '—', '参数中文名(水温/pH值等)'],
        ['value', 'DECIMAL(8,2)', '否', '—', '—', '触发告警时的参数值'],
        ['threshold_low', 'DECIMAL(8,2)', '是', '—', 'NULL', '告警下限阈值'],
        ['threshold_high', 'DECIMAL(8,2)', '是', '—', 'NULL', '告警上限阈值'],
        ['level', "ENUM('warning','error')", '否', '—', 'warning', '告警级别'],
        ['message', 'VARCHAR(255)', '否', '—', '—', '告警描述信息'],
        ['status', "ENUM('active','confirmed','ignored')", '否', '索引', 'active', '处理状态'],
        ['triggered_at', 'DATETIME(3)', '否', '—', '—', '告警触发时间'],
        ['confirmed_at', 'DATETIME(3)', '是', '—', 'NULL', '确认/忽略时间'],
        ['created_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '创建时间'],
    ]
)

tbl_caption('表8-9 alert_rules 告警规则配置表')
add_table(
    ['字段名', '类型', '允许为空', '键/索引', '默认值', '说明'],
    [
        ['id', 'BIGINT', '否', '主键', 'AUTO_INCREMENT', '规则ID'],
        ['param', 'VARCHAR(32)', '否', 'UNIQUE', '—', '参数名(waterTemp/ph/dissolvedOxygen/lightIntensity/conductivity)'],
        ['param_name', 'VARCHAR(32)', '否', '—', '—', '参数中文名(水温/pH值/溶解氧/光照/电导率)'],
        ['unit', 'VARCHAR(16)', '否', '—', '""', '单位(°C/mg/L/lux/μS/cm)'],
        ['min_val', 'DECIMAL(8,2)', '是', '—', 'NULL', '正常范围下限'],
        ['max_val', 'DECIMAL(8,2)', '是', '—', 'NULL', '正常范围上限'],
        ['threshold_low', 'DECIMAL(8,2)', '是', '—', 'NULL', '告警阈值下限'],
        ['threshold_high', 'DECIMAL(8,2)', '是', '—', 'NULL', '告警阈值上限'],
        ['level', 'VARCHAR(16)', '否', '—', 'warning', '告警级别(warning/error)'],
        ['enabled', 'TINYINT(1)', '否', '—', '1', '是否启用(0禁用/1启用)'],
        ['created_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '创建时间'],
        ['updated_at', 'DATETIME(3)', '否', '—', 'CURRENT_TIMESTAMP(3)', '更新时间'],
    ]
)

heading('8.4.2 物理设计', 3)
p('（一）数据库结构设计', bold=True, indent=False)
p('系统采用MySQL 8.0关系型数据库。数据库名为gexianmi，默认字符集为utf8mb4，排序规则为utf8mb4_general_ci。数据库引擎采用InnoDB，支持事务和外键约束。数据库文件存储在MySQL默认数据目录下。')

p('（二）表格设计', bold=True, indent=False)
p('根据逻辑设计中的表格结构，在MySQL 8.0中建立对应的物理表。表名和字段名均采用下划线命名（snake_case）。主键采用BIGINT AUTO_INCREMENT，外键字段与参照表主键类型保持一致。时间类型采用DATETIME(3)以支持毫秒精度。金额和参数类数值统一采用DECIMAL类型以保证精度。状态字段使用ENUM类型约束取值范围。')

p('（三）视图设计', bold=True, indent=False)
p('本系统暂不涉及复杂多表关联查询，业务层通过MyBatis-Plus的Service层封装处理跨表查询（如设备列表查询时自动填充大棚名称、大棚列表查询时自动统计设备数量），因此不单独设计数据库视图。')

p('（四）存储过程设计', bold=True, indent=False)
p('本系统的业务逻辑集中在Java Service层处理，数据库仅负责数据的存取。MyBatis-Plus的BaseMapper提供了完整的CRUD方法，条件查询通过LambdaQueryWrapper构建。暂不设计存储过程。')

p('（五）函数设计', bold=True, indent=False)
p('本系统暂不设计自定义数据库函数。数据统计和聚合操作在Java Service层完成。')

p('（六）数据库管理与安全设计', bold=True, indent=False)
p('数据库管理方面：通过MySQL用户权限体系控制访问，应用程序使用专用账号（root）连接，密码通过application.yml配置。建议生产环境创建受限权限的专用数据库用户。数据安全方面：用户密码采用BCrypt单向加密存储，不存储明文密码。定期备份数据库（建议每日全量备份），备份文件存储在与数据库服务器不同的物理位置。')

heading('8.5 系统接口设计', 2)

heading('8.5.1 外部接口', 3)
p('本系统涉及的外部接口主要包括传感器数据上报接口。传感器设备需支持HTTP协议，将采集到的环境数据以JSON格式POST至/api/environment/data接口。JSON请求体包含deviceId、timestamp、waterTemp、ph、dissolvedOxygen、lightIntensity、conductivity共7个字段。接口返回统一格式{code, data, msg}。')

heading('8.5.2 内部接口', 3)
p('本系统主要涉及的内部接口为前后端之间的RESTful API接口。前端通过Axios封装了两个HTTP客户端实例：envHttp（baseURL: /api/environment）用于环境数据相关请求，apiHttp（baseURL: /api）用于其他业务请求。所有内部接口均需携带JWT Token（登录和注册接口除外）。')

heading('8.5.3 接口清单', 3)

tbl_caption('表8-10 系统接口清单')
add_table(
    ['模块', '方法', '路径', '认证', '说明'],
    [
        ['认证', 'POST', '/api/auth/login', '否', '用户登录，返回JWT Token'],
        ['认证', 'POST', '/api/auth/register', '否', '用户注册'],
        ['认证', 'GET', '/api/auth/me', '是', '获取当前用户信息'],
        ['认证', 'PUT', '/api/auth/password', '是', '修改密码'],
        ['环境数据', 'POST', '/api/environment/data', '是', '上报一条环境数据'],
        ['环境数据', 'GET', '/api/environment/latest', '是', '查询最新一条数据'],
        ['环境数据', 'GET', '/api/environment/recent', '是', '查询最近N条数据'],
        ['环境数据', 'GET', '/api/environment/history', '是', '按时间范围查询历史数据'],
        ['环境数据', 'GET', '/api/environment/range', '是', '查询设备数据起止时间'],
        ['环境数据', 'GET', '/api/environment/devices', '是', '获取所有设备列表'],
        ['大棚', 'GET', '/api/greenhouses', '是', '列表（含设备数）'],
        ['大棚', 'POST', '/api/greenhouses', '是', '新建'],
        ['大棚', 'PUT', '/api/greenhouses/{id}', '是', '编辑'],
        ['大棚', 'DELETE', '/api/greenhouses/{id}', '是', '删除'],
        ['大棚', 'GET', '/api/greenhouses/{id}/devices', '是', '棚下设备'],
        ['设备', 'GET', '/api/devices', '是', '列表（含大棚名）'],
        ['设备', 'POST', '/api/devices', '是', '注册'],
        ['设备', 'PUT', '/api/devices/{code}', '是', '编辑'],
        ['设备', 'DELETE', '/api/devices/{code}', '是', '删除'],
        ['告警', 'GET', '/api/alerts', '是', '条件查询'],
        ['告警', 'GET', '/api/alerts/active', '是', '活跃告警'],
        ['告警', 'POST', '/api/alerts/{id}/confirm', '是', '确认单条'],
        ['告警', 'POST', '/api/alerts/{id}/ignore', '是', '忽略单条'],
        ['告警', 'POST', '/api/alerts/confirm-all', '是', '批量确认'],
        ['告警规则', 'GET', '/api/alert-rules', '是', '规则列表'],
        ['告警规则', 'PUT', '/api/alert-rules/{id}', '是', '更新阈值'],
    ]
)

page_break()

# ════════════════════ 9 开发环境配置 ════════════════════
heading('9 开发环境的配置', 1)
p('本系统的开发环境配置如下表所示：')

tbl_caption('表9-1 开发环境配置表')
add_table(
    ['类别', '工具/技术', '版本', '说明'],
    [
        ['操作系统', 'Windows 11', '—', '开发主机操作系统'],
        ['Java开发工具', 'IntelliJ IDEA', '2023.3+', 'Java后端开发IDE'],
        ['Java版本', 'JDK', '17 LTS', 'Java开发工具包'],
        ['构建工具', 'Maven', '3.9+', '依赖管理和项目构建'],
        ['后端框架', 'Spring Boot', '3.4.1', '内嵌Tomcat的Java Web框架'],
        ['ORM框架', 'MyBatis-Plus', '3.5.7', 'MyBatis增强工具包'],
        ['数据库', 'MySQL', '8.0', '关系型数据库'],
        ['数据库工具', 'DataGrip/Navicat', '—', '数据库可视化管理'],
        ['前端IDE', 'VS Code', '—', 'Vue前端开发编辑器'],
        ['前端框架', 'Vue 3', '3.5.x', 'Composition API语法'],
        ['UI组件库', 'Element Plus', '2.14.x', 'Vue 3 UI组件库'],
        ['图表库', 'ECharts', '6.1.x', 'JavaScript数据可视化库'],
        ['前端构建', 'Vite', '8.0.x', '前端构建和开发服务器'],
        ['Node.js', 'Node.js', '20.19+', 'JavaScript运行时环境'],
        ['版本控制', 'Git', '—', '代码版本管理（可选）'],
        ['Python环境', 'Anaconda', '—', '数据库初始化脚本运行环境'],
    ]
)

page_break()

# ════════════════════ 10 运行环境配置 ════════════════════
heading('10 运行环境的配置', 1)
p('本系统的运行环境配置如下表所示：')

tbl_caption('表10-1 运行环境配置表')
add_table(
    ['类别', '要求', '说明'],
    [
        ['服务器操作系统', 'Windows Server 2019+ / Linux (Ubuntu 20.04+ / CentOS 7+)', '支持Java 17运行'],
        ['Java运行时', 'JDK 17 LTS或以上', '后端运行必需'],
        ['数据库', 'MySQL 8.0', '数据持久化存储'],
        ['Web服务器', 'Spring Boot内嵌Tomcat（端口8000）', '后端应用容器'],
        ['反向代理（可选）', 'Nginx 1.20+', '生产环境建议配置反向代理和静态文件服务'],
        ['前端部署', 'Nginx静态文件服务 / Vite Preview', '生产环境构建产物dist/目录部署'],
        ['客户端浏览器', 'Chrome 90+ / Edge 90+ / Firefox 90+', '支持ES2020+和CSS Grid/Flexbox'],
        ['客户端分辨率', '≥1366×768（推荐1920×1080）', '大屏看板建议全高清或以上分辨率'],
        ['网络', '局域网', '传感器设备与服务器需在同一局域网内'],
    ]
)

page_break()

# ════════════════════ 11 测试环境配置 ════════════════════
heading('11 测试环境的配置', 1)
p('（1）一般地，单元测试、集成测试环境与开发环境相同。使用Spring Boot Test + JUnit 5进行后端单元测试，使用浏览器开发者工具进行前端功能验证。')
p('（2）一般地，系统测试、验收测试环境与运行环境相同或相似（更加严格）。测试应在与生产环境配置相同的服务器上进行，使用与生产数据规模相近的测试数据集。')

page_break()

# ════════════════════ 12 系统出错处理 ════════════════════
heading('12 系统出错处理', 1)
p('（1）出错信息：系统采用全局异常处理机制（GlobalExceptionHandler），对所有未捕获的异常进行统一拦截和包装。后端异常分为三类：参数校验失败返回HTTP 400和具体校验错误信息；业务逻辑异常（如用户名不存在、密码错误）返回HTTP 400和业务错误信息；服务器内部错误返回HTTP 500和简要错误描述。前端axios响应拦截器统一处理后端返回的错误，HTTP 401时自动清除Token并跳转登录页。')
p('（2）补救措施：数据库连接失败时，系统自动重试（HikariCP连接池配置connection-timeout为30秒）。API请求失败时，前端保持当前显示数据不变，不刷新空白页面。Token过期时自动引导用户重新登录。')
p('（3）系统维护设计：系统支持在不中断服务的情况下更新前端静态文件（替换dist/目录即可）。后端更新需重新打包和重启Spring Boot应用。数据库schema变更通过SQL脚本进行，保持向后兼容。')

page_break()

# ════════════════════ 13 测试基本内容 ════════════════════
heading('13 测试基本内容', 1)

heading('13.1 测试的参考文档', 2)
p('参考本文第4章业务需求分析、第5章系统功能需求、第7章非功能需求、第8章系统总体设计。')

heading('13.2 测试提交文档', 2)
p('测试阶段结束后，应提交以下文档：测试计划、测试用例清单、测试执行记录、缺陷报告和测试总结报告。')

heading('13.3 测试的进度及人员安排', 2)

tbl_caption('表13-1 测试进度及人员安排表')
add_table(
    ['阶段', '测试内容', '负责人', '时间安排', '完成标准'],
    [
        ['单元测试', '各Controller/Service方法功能验证', '开发人员', '编码阶段同步进行', '所有方法测试通过'],
        ['集成测试', '前后端接口联调、数据流验证', '开发人员', '编码完成后1-2天', '所有接口正常响应'],
        ['功能测试', '按用例逐一验证系统功能', '测试人员', '集成测试后2-3天', '全部用例执行通过'],
        ['用户界面测试', '页面布局、交互、响应式适配', '测试人员', '功能测试同期', '各分辨率显示正常'],
        ['验收测试', '按需求规格逐项验收', '项目负责人', '功能测试后1天', '需求覆盖率100%'],
    ]
)

heading('13.4 测试环境', 2)
p('软件环境：Windows 11操作系统，Chrome 120+浏览器，JDK 17，MySQL 8.0，Node.js 20.19+。')
p('硬件环境：CPU Intel Core i5及以上，内存16GB，硬盘可用空间50GB以上，显示器分辨率1920×1080。')

heading('13.5 测试工具', 2)

tbl_caption('表13-2 测试工具表')
add_table(
    ['工具名称', '用途', '版本'],
    [
        ['Postman', 'API接口手动测试', '最新版'],
        ['JUnit 5', 'Java单元测试框架', '5.11.x（随Spring Boot 3.4.1内置）'],
        ['Chrome DevTools', '前端调试、网络请求监控、性能分析', '浏览器内置'],
        ['MySQL Workbench / DataGrip', '数据库查询验证', '最新版'],
        ['MockMvc', 'Spring MVC测试（模拟HTTP请求）', '随Spring Boot Test内置'],
    ]
)

page_break()

# ════════════════════ 14 测试实施计划 ════════════════════
heading('14 测试实施计划', 1)

heading('14.1 接口测试', 2)

tbl_caption('表14-1 接口测试表（节选）')
add_table(
    ['接口', '方法', '测试输入', '预期输出'],
    [
        ['/api/auth/login', 'POST', '{"username":"admin","password":"admin123"}', 'code=200,返回token和userInfo'],
        ['/api/auth/login', 'POST', '{"username":"admin","password":"wrong"}', 'code=401,提示"用户名或密码错误"'],
        ['/api/auth/register', 'POST', '{"username":"test","password":"123456"}', 'code=200,提示"注册成功"'],
        ['/api/auth/register', 'POST', '{"username":"admin","password":"123456"}', 'code=400,提示"用户名已存在"'],
        ['/api/environment/latest', 'GET', '?deviceId=A-001', 'code=200,返回最新环境数据'],
        ['/api/environment/history', 'GET', '?deviceId=A-001&start=...&end=...', 'code=200,返回历史数据列表'],
        ['/api/greenhouses', 'GET', '无参数', 'code=200,返回大棚列表含设备数'],
        ['/api/greenhouses/1', 'PUT', '{"name":"测试大棚"}', 'code=200,返回更新后大棚信息'],
        ['/api/alert-rules', 'GET', '无参数', 'code=200,返回5条告警规则'],
        ['/api/alert-rules/1', 'PUT', '{"minVal":18,"maxVal":26}', 'code=200,返回更新后规则'],
        ['/api/alerts/active', 'GET', '无参数', 'code=200,返回活跃告警列表'],
        ['/api/alerts/1/confirm', 'POST', '无参数', 'code=200,告警状态变为confirmed'],
    ]
)

heading('14.2 集成测试', 2)
p('集成测试主要目的检测系统是否达到需求对业务流程及数据流的处理是否符合标准，检测系统对业务流处理是否存在逻辑不严谨及错误，检测需求是否存在不合理的标准及要求。此阶段测试基于功能完成的测试。')

tbl_caption('表14-2 集成测试表')
add_table(
    ['测试场景', '测试步骤', '预期结果'],
    [
        ['数据上报到展示的完整流程', '1.通过Postman模拟传感器POST数据\n2.刷新前端仪表盘页面\n3.切换到历史数据页查询', '仪表盘显示最新值，历史数据中可查到刚上报的记录'],
        ['用户注册到登录到访问业务页', '1.注册新用户\n2.用新用户登录\n3.访问实时监测页', '注册成功→登录成功→仪表盘正常展示数据'],
        ['大棚新增到设备绑定到数据查询', '1.新增大棚\n2.新增设备绑定到该大棚\n3.在监测页选择新大棚和设备', '大棚列表显示新大棚且设备数为1，监测页能选中新设备'],
    ]
)

heading('14.3 功能测试', 2)
p('对测试对象的功能测试应侧重于所有可直接追踪到用例或业务功能和业务规则的测试需求。这种测试的目标是核实数据的接受、处理和检索是否正确，以及业务规则的实施是否恰当。')

tbl_caption('表14-3 功能测试表（节选）')
add_table(
    ['功能模块', '测试用例', '输入/操作', '预期结果', '实际结果', '状态'],
    [
        ['登录', '正确用户名密码登录', 'admin/admin123', '跳转实时监测页，右上角显示admin', '', '待测'],
        ['登录', '错误密码登录', 'admin/wrong', '提示"用户名或密码错误"', '', '待测'],
        ['注册', '新用户注册', 'testuser/123456', '提示"注册成功"', '', '待测'],
        ['实时监测', '切换设备', '选择A-002', '仪表盘和迷你走势更新为A-002数据', '', '待测'],
        ['历史数据', '按时间查询', '选择24h快捷按钮', '曲线图展示最近24小时数据', '', '待测'],
        ['历史数据', '导出CSV', '点击导出按钮', '下载CSV文件，Excel可正常打开', '', '待测'],
        ['大棚管理', '搜索大棚', '输入"GH-A"', '列表仅显示GH-A大棚', '', '待测'],
        ['大棚管理', '编辑保存', '修改名称为"测试"', '提示"大棚信息已更新"', '', '待测'],
        ['告警规则', '修改阈值', '水温上限改为30', '保存后告警规则表更新', '', '待测'],
    ]
)

heading('14.4 用户界面测试', 2)
p('用户界面（UI）测试用于核实用户与软件之间的交互。UI测试的目标是确保用户界面会通过测试对象的功能来为用户提供相应的访问或浏览功能。测试内容包括：各页面在不同分辨率下的布局适应性、侧边栏折叠/展开功能、弹窗打开/关闭交互、表单校验提示信息的显示位置和时机、数据加载中loading动画、空数据时的占位提示。')

tbl_caption('表14-4 用户界面测试表')
add_table(
    ['测试项', '测试方法', '预期结果'],
    [
        ['响应式布局', '调整浏览器窗口至768px以下', '侧边栏变为浮层，仪表盘2列排列'],
        ['表单校验', '大棚表单留空必填项提交', '输入框下方显示红色校验提示'],
        ['加载状态', '切换设备时观察', '仪表盘区域显示loading骨架或保持上次数据'],
        ['空数据状态', '登录后切换至无数据设备', '仪表盘显示"等待数据"，历史页显示无数据提示'],
    ]
)

heading('14.5 性能测试', 2)

tbl_caption('表14-5 性能测试表')
add_table(
    ['测试项', '指标', '测试方法', '预期结果'],
    [
        ['API响应时间', '/api/environment/latest', '连续请求100次，取平均值', '平均响应时间<200ms'],
        ['API响应时间', '/api/environment/history（100条）', '查询7天数据', '平均响应时间<500ms'],
        ['API响应时间', '/api/greenhouses（列表）', '连续请求50次', '平均响应时间<100ms'],
        ['前端首屏加载', '首次访问/monitor页面', 'Chrome DevTools Performance', 'DOMContentLoaded < 2s'],
    ]
)

heading('14.6 安全性和访问控制测试', 2)

tbl_caption('表14-6 安全测试表')
add_table(
    ['测试项', '测试方法', '预期结果'],
    [
        ['未登录访问', '直接在浏览器输入/monitor', '自动跳转至/login'],
        ['Token过期', '修改localStorage中token为过期值', '请求返回401，自动跳转登录页'],
        ['注册接口免认证', '不携带Token调用POST /api/auth/register', '正常返回200（不受JWT拦截）'],
        ['登录页访问', '已登录时访问/login', '自动跳转至/monitor'],
        ['BCrypt密码安全', '查询数据库users表', 'password_hash字段存储的是BCrypt密文，非明文'],
    ]
)

heading('14.7 安装测试', 2)
p('安装测试有两个目的。第一个目的是确保该软件在正常情况和异常情况的不同条件下都能进行安装（首次安装、升级等）。第二个目的是核实软件在安装后可立即正常运行。测试内容包括：在Windows和Linux系统上分别执行数据库初始化脚本、使用mvn spring-boot:run启动后端、使用npm install && npm run dev启动前端、使用mvn clean package构建可部署的JAR包。')

heading('14.8 测试提交文档', 2)

tbl_caption('表14-8 测试提交文档清单')
add_table(
    ['文档名称', '内容说明', '作者'],
    [
        ['测试计划', '测试范围、策略、资源、进度安排', '测试负责人'],
        ['测试用例清单', '全部测试用例的汇总表', '测试人员'],
        ['测试执行记录', '每轮测试的实际执行结果', '测试人员'],
        ['缺陷报告', '发现的缺陷描述、严重程度、状态', '测试人员'],
        ['测试总结报告', '测试完成情况统计和质量评估', '测试负责人'],
    ]
)

heading('14.9 质量目标', 2)

tbl_caption('表14-9 质量目标表')
add_table(
    ['质量属性', '目标值', '度量方法'],
    [
        ['功能完整性', '100%', '按需求清单逐项检查'],
        ['接口可用率', '100%', '全部26个API接口可正常调用'],
        ['用例通过率', '≥95%', '已执行用例中通过的占比'],
        ['缺陷修复率', '100%', '已发现缺陷全部修复或标记为已知'],
    ]
)

page_break()

# ════════════════════ 15 测试报告 ════════════════════
heading('15 测试报告', 1)

heading('15.1 登录注册模块', 2)
heading('15.1.1 用户登录', 3)

tbl_caption('表15-1 用户登录测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-001-01', '正确账号密码登录', '用户名:admin,密码:admin123', '登录成功，跳转/monitor页面', '', '☐通过 ☐未通过'],
        ['TC-001-02', '错误密码登录', '用户名:admin,密码:wrong', '页面提示"用户名或密码错误"', '', '☐通过 ☐未通过'],
        ['TC-001-03', '空用户名', '用户名为空，密码任意', '表单校验提示"请输入用户名"', '', '☐通过 ☐未通过'],
        ['TC-001-04', '空密码', '用户名任意，密码为空', '表单校验提示"请输入密码"', '', '☐通过 ☐未通过'],
        ['TC-001-05', '回车登录', '输入正确信息后按Enter', '触发登录（同点击登录按钮）', '', '☐通过 ☐未通过'],
    ]
)

heading('15.1.2 用户注册', 3)

tbl_caption('表15-2 用户注册测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-002-01', '正常注册', '用户名:newuser,密码:123456,确认密码:123456', '提示"注册成功"', '', '☐通过 ☐未通过'],
        ['TC-002-02', '用户名重复', '用户名:admin', '提示"用户名已存在"', '', '☐通过 ☐未通过'],
        ['TC-002-03', '密码过短', '密码:12345', '提示"密码至少6位"', '', '☐通过 ☐未通过'],
        ['TC-002-04', '两次密码不一致', '密码:123456,确认:654321', '提示"两次密码不一致"', '', '☐通过 ☐未通过'],
    ]
)

heading('15.1.3 退出登录', 3)

tbl_caption('表15-3 退出登录测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-003-01', '退出登录', '点击右上角用户名→退出登录', '清除Token，跳转/login页', '', '☐通过 ☐未通过'],
    ]
)

heading('15.2 实时监测模块', 2)
heading('15.2.1 仪表盘展示', 3)

tbl_caption('表15-4 仪表盘测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-004-01', '默认设备数据', '登录后自动加载', '5个仪表盘显示A-001设备数据', '', '☐通过 ☐未通过'],
        ['TC-004-02', '切换设备', '下拉框选择A-002', '仪表盘数据更新为A-002设备', '', '☐通过 ☐未通过'],
        ['TC-004-03', '光照夜间模式', '查询夜间时间戳的数据', '仪表盘标题变为"光照(夜)"，范围0-200 lux', '', '☐通过 ☐未通过'],
        ['TC-004-04', '无设备时状态', '选择无数据的设备', '仪表盘显示"等待数据"', '', '☐通过 ☐未通过'],
    ]
)

heading('15.2.2 迷你走势', 3)

tbl_caption('表15-5 迷你走势测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-005-01', '走势图加载', '选择设备后观察', '迷你走势区展示5条彩色折线图', '', '☐通过 ☐未通过'],
        ['TC-005-02', '跳转历史页', '点击"完整历史曲线"按钮', '跳转/history并携带deviceId参数', '', '☐通过 ☐未通过'],
    ]
)

heading('15.3 历史数据模块', 2)

tbl_caption('表15-6 历史数据测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-006-01', '24h快捷查询', '点击24h按钮', '曲线图展示最近24小时数据', '', '☐通过 ☐未通过'],
        ['TC-006-02', '手动时间查询', '选择起止时间后查询', '曲线图展示所选范围数据', '', '☐通过 ☐未通过'],
        ['TC-006-03', 'CSV导出', '点击导出CSV按钮', '浏览器下载CSV文件，Excel可打开', '', '☐通过 ☐未通过'],
        ['TC-006-04', '无数据查询', '查询未来时间范围', '页面显示无数据提示', '', '☐通过 ☐未通过'],
    ]
)

heading('15.4 大棚与设备管理模块', 2)

tbl_caption('表15-7 大棚管理测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-007-01', '大棚列表加载', '进入大棚管理页', '表格展示所有大棚，含设备数', '', '☐通过 ☐未通过'],
        ['TC-007-02', '搜索过滤', '输入"GH-A"', '表格仅显示匹配行', '', '☐通过 ☐未通过'],
        ['TC-007-03', '新增大棚', '填写完整信息提交', '提示"大棚已创建"，列表刷新', '', '☐通过 ☐未通过'],
        ['TC-007-04', '编辑保存', '修改名称后保存', '提示"大棚信息已更新"', '', '☐通过 ☐未通过'],
        ['TC-007-05', '删除有设备的大棚', '点击删除GH-A', '提示"请先移除设备"', '', '☐通过 ☐未通过'],
    ]
)

tbl_caption('表15-8 设备管理测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-008-01', '设备列表加载', '进入设备管理页', '表格展示设备，所属大棚列为中文名称', '', '☐通过 ☐未通过'],
        ['TC-008-02', '新增设备', '填写信息并选择大棚', '提示"已创建"，列表刷新', '', '☐通过 ☐未通过'],
    ]
)

heading('15.5 告警模块', 2)

tbl_caption('表15-9 告警模块测试用例表')
add_table(
    ['用例编号', '测试项', '输入/操作', '预期结果', '实际结果', '测试状态'],
    [
        ['TC-009-01', '告警列表加载', '进入告警记录页', '表格展示告警记录', '', '☐通过 ☐未通过'],
        ['TC-009-02', '确认告警', '点击某条告警的"确认"', '该行状态变为"confirmed"', '', '☐通过 ☐未通过'],
        ['TC-009-03', '忽略告警', '点击某条告警的"忽略"', '该行状态变为"ignored"', '', '☐通过 ☐未通过'],
        ['TC-009-04', '批量确认', '点击"确认全部"', '所有活跃告警状态变为"confirmed"', '', '☐通过 ☐未通过'],
        ['TC-009-05', '规则列表', '进入告警规则页', '表格展示5条规则', '', '☐通过 ☐未通过'],
        ['TC-009-06', '修改阈值', '修改水温阈值为18-26', '提示"规则已更新"，列表刷新', '', '☐通过 ☐未通过'],
    ]
)

page_break()

# ════════════════════ 16 交付 ════════════════════
heading('16 交付', 1)
p('详细写明需要交付的清单项。交付清单表如下表所示。')

tbl_caption('表16-1 交付清单表')
add_table(
    ['序号', '交付项', '格式', '说明'],
    [
        ['1', '葛仙米环境监测系统——源代码', 'ZIP压缩包', '包含java_backend和frontend两个目录的全部源代码'],
        ['2', '数据库初始化脚本', 'SQL文件', 'init_users.sql和init_alert_rules.sql'],
        ['3', 'Python数据初始化脚本', 'Python文件', 'init_db.py和clean_and_seed.py'],
        ['4', '系统设计说明书（本文档）', 'DOCX/PDF', '完整的需求分析和系统设计文档'],
        ['5', '前端README文档', 'MD文件', '前端技术栈、目录结构、启动方式说明'],
        ['6', '后端README文档', 'MD文件', '后端技术栈、接口清单、数据库设计说明'],
        ['7', '可部署JAR包（可选）', 'JAR文件', 'mvn package构建的Fat JAR'],
    ]
)

page_break()

# ════════════════════ 17 总结与展望 ════════════════════
heading('17 总结与展望', 1)

heading('17.1 总结（每位同学需写自己的总结）', 2)
p('本文所完成的主要工作有如下几方面：')
p('1. 对葛仙米养殖环境监测的业务现状进行了深入调研和分析，识别了传统手工记录方式存在的问题，提出了基于Web的信息化监测方案。通过业务流程图（活动图）描述了优化前后的业务流程，使数据采集从人工变为全自动、告警从被动发现变为主动推送。')
p('2. 采用UML建模技术对系统功能需求进行了详细分析。识别了三类参与者（管理员、操作员、观察者），绘制了系统总用例图，对7个子系统共8个核心用例进行了详细的用例描述。明确了系统的功能边界和用户交互方式。')
p('3. 完成了系统的总体设计。采用B/S四层架构（表示层-控制层-业务层-数据层），基于Spring Boot + Vue 3技术栈。设计了7个功能模块，绘制了功能包图、各模块类图和核心时序图。')
p('4. 完成了数据库的逻辑设计和物理设计。识别了6个核心实体，绘制了E-R图和实体属性图。设计了6张数据表的完整结构，明确了字段类型、约束和索引。建立了完整的接口清单（26个RESTful API端点）。')
p('5. 制定了完整的测试计划，设计了涵盖功能测试、接口测试、集成测试、用户界面测试、安全测试和性能测试的测试用例。')
p('通过对葛仙米生长环境智能监测系统的业务、功能、数据分析的过程，不仅使我深入了解了一个完整Web应用从需求分析到系统设计的全流程，而且也使我学到了前后端分离架构的设计方法和UML建模技术的实际应用。由于此次负责业务分析、功能分析和数据分析的全过程，使我对软件系统开发的需求工程有了更深的认识。')

heading('17.2 展望', 2)
p('虽然本文完整地分析并设计了葛仙米生长环境智能监测系统，但是系统的功能仍然存在不足之处：')
p('1. 理论结合实际。本文只是对葛仙米监测系统进行的研究与分析，在实际项目中可能会遇到本文分析之外的功能，今后在实际项目中不断完善。')
p('2. 系统当前仅实现了Web端，后续可扩展移动端App或微信小程序，使养殖人员通过手机即可查看数据和接收告警推送。')
p('3. 告警规则的触发条件目前仅支持简单的阈值比较，未来可引入机器学习模型，基于历史数据趋势进行预测性告警。')
p('4. 图像识别功能可作为第三阶段扩展——通过水下摄像头定时拍摄葛仙米图像，利用AI模型自动判断生长阶段和健康状况。')
p('5. 硬件联动功能可作为第二阶段扩展——系统根据告警规则自动向执行器发送控制指令，实现环境参数的自动调控闭环。')
p('6. 目前系统的用户权限仅通过角色进行粗粒度控制，后续可引入RBAC权限模型，支持更细粒度的权限管理。')
p('7. 操作日志功能可进一步增强系统的可追溯性和安全性。')

page_break()

# ════════════════════ 参考文献 ════════════════════
heading('参考文献', 1)
refs = [
    '[1] 百度百科. 科教兴国战略[EB/OL]. http://baike.baidu.com/view/170037.htm, 2014.12.18.',
    '[2] Jackson M. Software Requirements & Specifications: A Lexicon of Practice, Principles and Prejudices. Harlow: Addison-Wesley, 1995.',
    '[3] 周苏, 王文. 软件工程学教程[M]. 科学出版社, 2003.',
    '[4] 齐治昌, 谭庆平, 宁洪. 软件工程(第二版)[M]. 高等教育出版社, 2004.',
    '[5] 王珊, 陈红. 数据库系统原理教程[M]. 清华大学出版社, 1998.',
    '[6] 李刚. 轻量级Java EE企业应用实战（第5版）[M]. 电子工业出版社, 2018.',
    '[7] 尤雨溪. Vue.js官方文档[EB/OL]. https://cn.vuejs.org/, 2026.',
    '[8] Spring Boot Reference Documentation[EB/OL]. https://docs.spring.io/spring-boot/docs/current/reference/html/, 2026.',
    '[9] MyBatis-Plus官方文档[EB/OL]. https://baomidou.com/, 2026.',
    '[10] Apache ECharts官方文档[EB/OL]. https://echarts.apache.org/, 2026.',
    '[11] Element Plus官方文档[EB/OL]. https://element-plus.org/, 2026.',
    '[12] 葛仙米人工养殖技术研究进展[J]. 水产学报, 2020.',
]
for ref in refs:
    p(ref, indent=False, size=11)

# ── 保存 ──
out_path = r'C:\Users\bby\Desktop\GXM_DigitalTwin\GeXianMI\实训总结报告-葛仙米环境监测系统-v2.docx'
doc.save(out_path)
print(f'OK: {out_path}')
