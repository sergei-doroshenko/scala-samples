call curl -d "{\"name\":\"Mr. Fergusson\"}" -H "Content-Type: application/json" -X POST localhost:8080/teachers
call curl -d {\"name\":\"Mathimatics\",\"teacherId\":1} -H "Content-Type: application/json" -X POST localhost:8080/courses
call curl -d "{\"name\":\"Computer since\",\"teacherId\":1}" -H "Content-Type: application/json" -X POST localhost:8080/courses
call curl localhost:8080/courses
call curl -d {\"name\":\"Adam\"} -H "Content-Type: application/json" -X POST localhost:8080/students
call curl localhost:8080/students
call curl -d {\"courseId\":1} -H "Content-Type: application/json" -X POST localhost:8080/students/1/courses
call curl localhost:8080/students
call curl -d "{\"courseId\":1,\"feedback\":\"Cool work!\"}" -H "Content-Type: application/json" -X PUT localhost:8080/students/1/courses
call curl localhost:8080/students