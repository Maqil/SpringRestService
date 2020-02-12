package schoolservice.demo;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private Date birthDate;
    @ManyToOne
    private Laboratory laboratory;
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class Laboratory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    @Size(min = 2, max = 30)
    private String name;
    private String contact;
    @OneToMany(mappedBy = "laboratory", fetch = FetchType.EAGER)
    private Collection<Student> students;
}

@Projection(name = "p1", types = Student.class)
interface StudentProjection {
    public String getEmail();

    public String getName();

    public Laboratory getLaboratory();
}

@RepositoryRestResource
interface StudentRepository extends JpaRepository<Student, Long> {
    @RestResource(path = "/byName")
    public List<Student> findByNameContains(@Param(value = "mc") String mc);
}

@RepositoryRestResource
interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
}

@RestController
@RequestMapping("/api")
class SchoolRestController {
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    LaboratoryRepository laboratoryRepository;

    @GetMapping("/students")
    public List<Student> students() {
        return studentRepository.findAll();
    }

    @GetMapping("/students/{id}")
    public Student getOne(@PathVariable(name = "id") Long id) {
        return studentRepository.findById(id).get();
    }

    @PostMapping("/students")
    public Student save(@RequestBody Student student) {
        if (student.getLaboratory().getId() == null) {
            Laboratory laboratory = laboratoryRepository.save(student.getLaboratory());
            student.setLaboratory(laboratory);
        }
        return studentRepository.save(student);
    }

    @PutMapping("/students/{id}")
    public Student update(@PathVariable(name = "id") Long id, @RequestBody Student student) {
        student.setId(id);
        return studentRepository.save(student);
    }


    @DeleteMapping("/students/{id}")
    public void delete(@PathVariable(name = "id") Long id) {
        studentRepository.deleteById(id);
    }
}

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner start(StudentRepository studentRepository, RepositoryRestConfiguration restConfiguration, LaboratoryRepository laboratoryRepository) {
        return args -> {
            restConfiguration.exposeIdsFor(Student.class);

            Laboratory l1 = laboratoryRepository.save(new Laboratory(null, "dev", "dev@gm", null));
            Laboratory l2 = laboratoryRepository.save(new Laboratory(null, "dev", "dev@gm", null));

            studentRepository.save(new Student(null, "Hid", "hid@gmail.com", new Date(), l1));
            studentRepository.save(new Student(null, "oiu", "oiu@gmail.com", new Date(), l1));
            studentRepository.save(new Student(null, "sdsd", "sdsd@gmail.com", new Date(), l2));

//            studentRepository.findAll().forEach(System.out::println);
        };

    }
}
