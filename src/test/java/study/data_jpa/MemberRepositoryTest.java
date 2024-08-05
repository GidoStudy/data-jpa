package study.data_jpa;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.data_jpa.dto.MemberDto;
import study.data_jpa.entity.Member;
import study.data_jpa.entity.Team;
import study.data_jpa.repository.MemberJpaRepository;
import study.data_jpa.repository.MemberRepository;
import study.data_jpa.repository.TeamRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 14);
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    void findMember() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(member);
    }

    @Test
    void findUsernameList() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        assertThat(usernameList.size()).isEqualTo(2);
        assertThat(usernameList.get(0)).isEqualTo("AAA");
        assertThat(usernameList.get(1)).isEqualTo("BBB");
    }

    @Test
    void findMemberDto() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member = new Member("AAA", 10,teamA);
        Member member2 = new Member("BBB", 20,teamA);
        Member member3 = new Member("CCC", 30,teamB);
        Member member4 = new Member("DDD", 40,teamB);
        memberRepository.save(member);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member m : byNames) {
            System.out.println("m = " + m);
        }
    }

    @Test
    void returnTypeTest() {
        Member member = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");
        Member member1 = aaa2.orElseThrow(() -> new NullPointerException("없습니다"));
        for (Member member3 : aaa) {
            System.out.println("member3 = " + member3);
        }
        System.out.println("aaa1 = " + aaa1);
        System.out.println("member1 = " + member1);
    }

    @Test
    void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        PageRequest pageRequest = PageRequest.of(
                0, 3, Sort.by(Sort.Direction.DESC, "username"));
        int age=10;
        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        Page<MemberDto> map = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();
        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.team = " + member.getTeam());
        }

    }

    @Test
    void queryHint() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");
        em.flush();
    }

    @Test
    void Lock() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByusername("member1");
    }

    @Test
    void custom() {
        List<Member> memberCustom = memberRepository.findMemberCustom();
    }
}
