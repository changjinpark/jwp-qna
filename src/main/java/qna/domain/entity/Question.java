package qna.domain.entity;

import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import qna.domain.aggregate.AnswerGroup;
import qna.domain.aggregate.DeleteHistoryGroup;
import qna.domain.vo.Contents;
import qna.domain.vo.Title;
import qna.exception.CannotDeleteException;

@Entity
public class Question extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private Contents contents;

	@Column(nullable = false)
	private boolean deleted = false;

	@Embedded
	private Title title;

	@ManyToOne
	@JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_question_writer"))
	private User writer;

	@Embedded
	private AnswerGroup answerGroup;

	protected Question() {
	}

	private Question(Long id, String title, String contents) {
		this.id = id;
		this.title = Title.generate(title);
		this.contents = Contents.generate(contents);
		this.answerGroup = AnswerGroup.generate();
	}

	public static Question generate(String title, String contents) {
		return generate(null, title, contents);
	}

	public static Question generate(Long id, String title, String contents) {
		return new Question(id, title, contents);
	}

	public Long id() {
		return id;
	}

	public User writer() {
		return writer;
	}

	public List<Answer> answers() {
		return answerGroup.answers();
	}

	public Question writeBy(User writer) {
		this.writer = writer;
		updatedAtNow();
		return this;
	}

	public boolean isOwner(User writer) {
		return this.writer.equals(writer);
	}

	public void addAnswer(Answer answer) {
		if (!answers().contains(answer)) {
			answers().add(answer);
		}
		answer.question(this);
		updatedAtNow();
	}

	public void removeAnswer(Answer answer) {
		answers().remove(answer);
		updatedAtNow();
	}

	public boolean isDeleted() {
		return deleted;
	}

	public DeleteHistoryGroup delete(User loginUser) {
		DeleteHistoryGroup deleteHistoryGroup = DeleteHistoryGroup.generate();
		deleteHistoryGroup.add(deleteQuestion(loginUser));
		deleteHistoryGroup.addAll(answerGroup.deleteAll(loginUser));
		return deleteHistoryGroup;
	}

	private DeleteHistory deleteQuestion(User loginUser) {
		checkQuestionIsSameWithUserAndWriter(loginUser);
		this.deleted = true;
		updatedAtNow();
		return DeleteHistory.ofQuestion(id, writer);
	}

	private void checkQuestionIsSameWithUserAndWriter(User loginUser) {
		if (!isOwner(loginUser)) {
			throw new CannotDeleteException("질문 삭제는 작성자만 가능합니다.");
		}
	}

	@Override
	public String toString() {
		return "Question{" +
			"id=" + id +
			", contents='" + contents + '\'' +
			", deleted=" + deleted +
			", title='" + title + '\'' +
			", writer=" + writer +
			'}';
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof Question)) {
			return false;
		}
		Question question = (Question) object;
		return deleted == question.deleted && Objects.equals(id, question.id)
			&& Objects.equals(contents, question.contents) && Objects.equals(title, question.title)
			&& writer.equals(question.writer) && answerGroup.containsAll(question.answerGroup)
			&& question.answerGroup.containsAll(answerGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, contents, deleted, title, writer, answerGroup);
	}
}