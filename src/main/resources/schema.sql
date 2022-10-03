DROP TABLE IF EXISTS employee;
CREATE TABLE employee
(
    employeeId int not null primary key,
    name       VARCHAR(255),
    department VARCHAR(255),
    age        int

);
insert into employee (employeeId, name, department, age)
values (1, 'john doe', 'sales', 25);
insert into employee (employeeId, name, department, age)
values (2, 'bob builder', 'business', 50);
insert into employee (employeeId, name, department, age)
values (3, 'naruto uzumaki', 'technical', 20);
insert into employee (employeeId, name, department, age)
values (4, 'sasuke uchiha', 'technical', 20);