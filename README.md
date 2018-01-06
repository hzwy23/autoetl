## autoetl介绍
通过Excel模板配置的方式，实现ETL数据加载，清洗转换。

## Excel模板介绍
模板路径在ExcelTemplate目录中，贷款数据映射.xlsx，程序通过解析模板生成存储过程。

## 使用方法：
启动服务后，在浏览器中输入：
```aidl
http://localhost:8023/?fileScript=模板绝对路径
```
在解析的模板目录中将会生成一个.sql文件结尾的脚本，这个就是根据模板生成的ETL存储过程
