package com.example.jsh_project.Service;

import com.example.jsh_project.domain.Dto.request.MemberDto;
import com.example.jsh_project.domain.Dto.request.MemberRegisterRequest;
import com.example.jsh_project.domain.Entity.Member;
import com.example.jsh_project.repository.MemberRepository;
import com.example.jsh_project.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MailService mailService;
    @Value("${jwt.token.secret}")
    private String secretkey;
    private final long expireTimeMs = 1000 * 60 * 60 * 24 * 7; // 토큰 7일

    public MemberDto register(MemberRegisterRequest request) {
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    throw new RuntimeException();
                });

        memberRepository.findByEmployName(request.getEmployName())
                .ifPresent(member ->{
                    throw new RuntimeException();
                });


        Member saveMember = memberRepository.save(request.toEntity(bCryptPasswordEncoder.encode(request.getPassword())));
        return MemberDto.fromEntity(saveMember);
    }

    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 사원입니다."));

        if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return JwtUtil.createToken(email, expireTimeMs, secretkey);
    }

    public String getPwd(String email){
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 사용자입니다."));

        return member.getPassword();
    }
    public void modifyPwd(MemberDto memberDto) throws Exception{
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pwd = encoder.encode(memberDto.getPassword());


    }
}
