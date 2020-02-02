## MySQL刷题

### Q1: 组合两个表

表1: Person

```sql
+-------------+---------+
| 列名         | 类型    |
+-------------+---------+
| PersonId    | int     |
| FirstName   | varchar |
| LastName    | varchar |
+-------------+---------+
```

PersonId 是上表主键

表2: Address

```sql
+-------------+---------+
| 列名         | 类型    |
+-------------+---------+
| AddressId   | int     |
| PersonId    | int     |
| City        | varchar |
| State       | varchar |
+-------------+---------+
```

AddressId 是上表主键

编写一个 SQL 查询，满足条件：无论 person 是否有地址信息，都需要基于上述两表提供 person 的以下信息：

```sql
FirstName, LastName, City, State
```

解法:

```sql
select FirstName, LastName, City, State from Person left outer join Address on Person.PersonId = Address.PersonId;
```

### Q2: 第二高的薪水

编写一个 SQL 查询，获取 Employee 表中第二高的薪水（Salary） 。

```sql
+----+--------+
| Id | Salary |
+----+--------+
| 1  | 100    |
| 2  | 200    |
| 3  | 300    |
+----+--------+
```

例如上述 Employee 表，SQL查询应该返回 200 作为第二高的薪水。如果不存在第二高的薪水，那么查询应返回 null。

```sql
+---------------------+
| SecondHighestSalary |
+---------------------+
| 200                 |
+---------------------+
```

* 解法一:

```sql
select(
    select distinct Salary from Employee
    order by Salary desc limit 1 offset 1
)as SecondHighestSalary;
```

* 解法二:

```sql
select
    ifnull(
        (select Salary from Employee order by Salary desc limit 1 offset 1), null
    )
as SecondHighestSalary;
```

### Q3: 第N高的薪水

编写一个 SQL 查询，获取 Employee 表中第 n 高的薪水（Salary）。

```sql
+----+--------+
| Id | Salary |
+----+--------+
| 1  | 100    |
| 2  | 200    |
| 3  | 300    |
+----+--------+
```

例如上述 Employee 表，n = 2 时，应返回第二高的薪水 200。如果不存在第 n 高的薪水，那么查询应返回 null。

```sql
+------------------------+
| getNthHighestSalary(2) |
+------------------------+
| 200                    |
+------------------------+
```

* 解法一:

```sql
CREATE FUNCTION getNthHighestSalary(N INT) RETURNS INT
BEGIN
    SET N = N - 1;
    RETURN (
    # Write your MySQL query statement below.
        SELECT IFNULL(
            (SELECT DISTINCT Salary FROM Employee ORDER BY Salary DESC LIMIT N, 1), NULL
        ) AS getNthHighestSalary
    );
END
```

### Q4: 分数排名

编写一个 SQL 查询来实现分数排名。如果两个分数相同，则两个分数排名（Rank）相同。请注意，平分后的下一个名次应该是下一个连续的整数值。换句话说，名次之间不应该有“间隔”。

```sql
+----+-------+
| Id | Score |
+----+-------+
| 1  | 3.50  |
| 2  | 3.65  |
| 3  | 4.00  |
| 4  | 3.85  |
| 5  | 4.00  |
| 6  | 3.65  |
+----+-------+
```

例如，根据上述给定的 Scores 表，你的查询应该返回（按分数从高到低排列）：

```sql
+-------+------+
| Score | Rank |
+-------+------+
| 4.00  | 1    |
| 4.00  | 1    |
| 3.85  | 2    |
| 3.65  | 3    |
| 3.65  | 3    |
| 3.50  | 4    |
+-------+------+
```

* 解法一:

```sql
select
    a.Score as Score,
    ifnull(
        (select count(distinct b.Score) from Scores as b where b.Score >= a.Score), null
    ) as Rank
from Scores as a order by Score desc;
```

### Q5: 连续出现的数字

编写一个 SQL 查询，查找所有至少连续出现三次的数字。

```sql
+----+-----+
| Id | Num |
+----+-----+
| 1  |  1  |
| 2  |  1  |
| 3  |  1  |
| 4  |  2  |
| 5  |  1  |
| 6  |  2  |
| 7  |  2  |
+----+-----+
```

例如，给定上面的 Logs 表， 1 是唯一连续出现至少三次的数字。

```sql
+-----------------+
| ConsecutiveNums |
+-----------------+
| 1               |
+-----------------+
```

* 解法一:

```sql
select distinct a.Num as ConsecutiveNums
from
Logs a, Logs b, Logs c
where
b.Id = a.Id + 1 and a.Num = b.Num
and
c.Id = b.Id + 1 and b.Num = c.Num;
```

### Q6: 超过经理工资的员工

Employee 表包含所有员工，他们的经理也属于员工。每个员工都有一个 Id，此外还有一列对应员工的经理的 Id。

```sql
+----+-------+--------+-----------+
| Id | Name  | Salary | ManagerId |
+----+-------+--------+-----------+
| 1  | Joe   | 70000  | 3         |
| 2  | Henry | 80000  | 4         |
| 3  | Sam   | 60000  | NULL      |
| 4  | Max   | 90000  | NULL      |
+----+-------+--------+-----------+
```

给定 Employee 表，编写一个 SQL 查询，该查询可以获取收入超过他们经理的员工的姓名。在上面的表格中，Joe 是唯一一个收入超过他的经理的员工。

```sql
+----------+
| Employee |
+----------+
| Joe      |
+----------+
```

* 解法一:

```sql
select a.Name as Employee
from Employee a, Employee b
where
a.ManagerId = b.Id and a.Salary > b.Salary;
```

### Q7: 重复的邮箱

编写一个 SQL 查询，查找 Person 表中所有重复的电子邮箱。

示例：

```sql
+----+---------+
| Id | Email   |
+----+---------+
| 1  | a@b.com |
| 2  | c@d.com |
| 3  | a@b.com |
+----+---------+
```

根据以上输入，你的查询应返回以下结果：

```sql
+---------+
| Email   |
+---------+
| a@b.com |
+---------+
```

* 解法一:

```sql
select distinct p1.Email as Email
from Person p1, Person p2
where
p1.Email = p2.Email and p1.Id != p2.Id;
```

### Q8: 从不订购的客人

某网站包含两个表，Customers 表和 Orders 表。编写一个 SQL 查询，找出所有从不订购任何东西的客户。

Customers 表：

```sql
+----+-------+
| Id | Name  |
+----+-------+
| 1  | Joe   |
| 2  | Henry |
| 3  | Sam   |
| 4  | Max   |
+----+-------+
```

Orders 表：

```sql
+----+------------+
| Id | CustomerId |
+----+------------+
| 1  | 3          |
| 2  | 1          |
+----+------------+
```

例如给定上述表格，你的查询应返回：

```sql
+-----------+
| Customers |
+-----------+
| Henry     |
| Max       |
+-----------+
```

* 解法一:

```sql
select Name as Customers
from Customers
where Id not in
(select distinct CustomerId from Orders);
```

### Q9: 部门最高的工资

Employee 表包含所有员工信息，每个员工有其对应的 Id, salary 和 department Id。

```sql
+----+-------+--------+--------------+
| Id | Name  | Salary | DepartmentId |
+----+-------+--------+--------------+
| 1  | Joe   | 70000  | 1            |
| 2  | Henry | 80000  | 2            |
| 3  | Sam   | 60000  | 2            |
| 4  | Max   | 90000  | 1            |
+----+-------+--------+--------------+
```

Department 表包含公司所有部门的信息。

```sql
+----+----------+
| Id | Name     |
+----+----------+
| 1  | IT       |
| 2  | Sales    |
+----+----------+
```

编写一个 SQL 查询，找出每个部门工资最高的员工。例如，根据上述给定的表格，Max 在 IT 部门有最高工资，Henry 在 Sales 部门有最高工资。

```sql
+------------+----------+--------+
| Department | Employee | Salary |
+------------+----------+--------+
| IT         | Max      | 90000  |
| Sales      | Henry    | 80000  |
+------------+----------+--------+
```

* 解法一：

```sql
select
Department.Name as Department,
Employee.Name as Employee,
Employee.Salary as Salary
from
Employee
join Department
on Employee.DepartmentId = Department.Id
where
(Employee.DepartmentId, Salary)
in
(select DepartmentId, Max(Salary)
from Employee group by DepartmentId);
```

### Q10: 部门工资前三高的所有员工

Employee 表包含所有员工信息，每个员工有其对应的工号 Id，姓名 Name，工资 Salary 和部门编号 DepartmentId 。

```sql
+----+-------+--------+--------------+
| Id | Name  | Salary | DepartmentId |
+----+-------+--------+--------------+
| 1  | Joe   | 85000  | 1            |
| 2  | Henry | 80000  | 2            |
| 3  | Sam   | 60000  | 2            |
| 4  | Max   | 90000  | 1            |
| 5  | Janet | 69000  | 1            |
| 6  | Randy | 85000  | 1            |
| 7  | Will  | 70000  | 1            |
+----+-------+--------+--------------+
```

Department 表包含公司所有部门的信息。

```sql
+----+----------+
| Id | Name     |
+----+----------+
| 1  | IT       |
| 2  | Sales    |
+----+----------+
```

编写一个 SQL 查询，找出每个部门获得前三高工资的所有员工。例如，根据上述给定的表，查询结果应返回：

```sql
+------------+----------+--------+
| Department | Employee | Salary |
+------------+----------+--------+
| IT         | Max      | 90000  |
| IT         | Randy    | 85000  |
| IT         | Joe      | 85000  |
| IT         | Will     | 70000  |
| Sales      | Henry    | 80000  |
| Sales      | Sam      | 60000  |
+------------+----------+--------+
```

解释：

IT 部门中，Max 获得了最高的工资，Randy 和 Joe 都拿到了第二高的工资，Will 的工资排第三。销售部门（Sales）只有两名员工，Henry 的工资最高，Sam 的工资排第二。

* 解法一:

```sql
SELECT
Department.NAME AS Department,
e1.NAME AS Employee,
e1.Salary AS Salary
FROM
Employee AS e1,Department
WHERE
e1.DepartmentId = Department.Id
AND 3 > (SELECT  count( DISTINCT e2.Salary )
FROM Employee AS e2
WHERE e1.Salary < e2.Salary AND e1.DepartmentId = e2.DepartmentId)
ORDER BY Department.NAME,Salary DESC;
```

### Q11: 删除重复的电子邮箱

编写一个 SQL 查询，来删除 Person 表中所有重复的电子邮箱，重复的邮箱里只保留 Id 最小 的那个。

```sql
+----+------------------+
| Id | Email            |
+----+------------------+
| 1  | john@example.com |
| 2  | bob@example.com  |
| 3  | john@example.com |
+----+------------------+
```

Id 是这个表的主键。

例如，在运行你的查询语句之后，上面的 Person 表应返回以下几行:

```sql
+----+------------------+
| Id | Email            |
+----+------------------+
| 1  | john@example.com |
| 2  | bob@example.com  |
+----+------------------+
```

提示：

* 执行 SQL 之后，输出是整个 Person 表。
* 使用 delete 语句。

* 解法一:

```sql
delete
p1
from Person p1, Person p2
where
p1.Email = p2.Email
and
p1.Id > p2.Id;
```

### Q12: 增加的温度

给定一个 Weather 表，编写一个 SQL 查询，来查找与之前（昨天的）日期相比温度更高的所有日期的 Id。

```sql
+---------+------------------+------------------+
| Id(INT) | RecordDate(DATE) | Temperature(INT) |
+---------+------------------+------------------+
|       1 |       2015-01-01 |               10 |
|       2 |       2015-01-02 |               25 |
|       3 |       2015-01-03 |               20 |
|       4 |       2015-01-04 |               30 |
+---------+------------------+------------------+
```

例如，根据上述给定的 Weather 表格，返回如下 Id:

```sql
+----+
| Id |
+----+
|  2 |
|  4 |
+----+
```

* 解法一:

```sql
select 
a.Id
from
Weather a, Weather b
where
datediff(a.RecordDate, b.RecordDate) = 1
and
a.Temperature > b.Temperature;
```
