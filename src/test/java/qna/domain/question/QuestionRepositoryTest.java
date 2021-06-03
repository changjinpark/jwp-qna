package qna.domain.question;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import qna.domain.user.User;
import qna.domain.user.UserRepository;
import qna.domain.user.UserTest;

@DirtiesContext
@DataJpaTest
class QuestionRepositoryTest {

	@Autowired
	QuestionRepository repository;

	@Autowired
	UserRepository userRepository;

	@Test
	void saveAndFind() {
		User user = userRepository.save(UserTest.JAVAJIGI);
		Question q1 = QuestionTest.Q1.writeBy(user);
		Question actual = repository.save(q1);
		Question expect = repository.findById(actual.getId()).orElseThrow(EntityNotFoundException::new);

		assertThat(actual).isEqualTo(expect);
	}

	@Test
	@DisplayName("Deleted 가 False 인 모든 Question 조회")
	void findByDeletedFalseTest() {
		User user = userRepository.save(UserTest.JAVAJIGI);
		Question q1 = QuestionTest.Q1.writeBy(user);
		Question q2 = QuestionTest.Q2.writeBy(user);
		q1.setDeleted(false);
		q2.setDeleted(true);
		repository.save(q1);
		repository.save(q2);

		List<Question> questions = repository.findAll();
		assertThat(questions).containsExactly(q1);
	}

	@Test
	@DisplayName("Deleted 가 False 인 특정 Question 조회")
	void findByIdAndDeletedFalseTest() {
		User sanjigi = UserTest.SANJIGI;
		User save = userRepository.save(sanjigi);

		Question q1 = QuestionTest.Q1.writeBy(save);
		q1.setDeleted(false);
		Question save1 = repository.save(q1);

		Question question = repository.findById(save1.getId()).orElseThrow(EntityNotFoundException::new);
		assertThat(question).isEqualTo(save1);

		Question q2 = QuestionTest.Q2.writeBy(save);
		q2.setDeleted(true);

		assertThat(repository.findAll()).hasSize(1);

	}
}