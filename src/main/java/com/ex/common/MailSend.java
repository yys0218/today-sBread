package com.ex.common;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Data
public class MailSend {

  private final JavaMailSender mailSender;

  // HTML 메일을 전송하는 메서드
  // 매개변수 : 수신자 이메일(to), 메일 제목(subject), 메일 본문(htmlContent)
  public String sendEmailCodeMail(String to, String subject) throws MessagingException {
    // [1] MimeMessage 객체 생성
    // - HTML 메일, 첨부파일 등을 보낼 수 있는 고급 메시지 형식
    MimeMessage message = mailSender.createMimeMessage();

    // [2] MimeMessageHelper 생성
    // - (message, multipart여부, 인코딩)
    // - 두 번째 파라미터 true : 멀티파트(첨부파일) 허용
    // - 세 번째 파라미터 "UTF-8" : 한글 깨짐 방지
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // [3] 수신자 지정
    helper.setTo(to);

    // [4] 메일 제목 지정
    helper.setSubject(subject);

    // [5] 메일 본문 지정
    // 두 번째 파라미터 true : 본문이 HTML 형식임을 지정
    String code = emailCode();
    String htmlContent = """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
        <meta charset="UTF-8">
        <style>
          body {
            margin: 0;
            padding: 0;
            background-color: #f3f4f6;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          }
          .mail-wrapper {
            width: 100%%;
            padding: 40px 0;
            background-color: #f3f4f6;
          }
          .mail-container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            border: 1px solid #e5e7eb;
          }
          .mail-header {
            background-color: #4a76a8;
            padding: 20px;
            text-align: center;
            color: #ffffff;
            font-size: 24px;
            font-weight: bold;
          }
          .mail-body {
            padding: 30px;
            font-size: 16px;
            color: #333333;
            line-height: 1.6;
          }
          .highlight {
            font-weight: bold;
            color: #4a76a8;
          }
          .code-box {
            display: block;            /* block으로 만들어야 margin auto가 먹힘 */
            margin: 20px auto;         /* 위아래 20px, 좌우 auto로 가운데 정렬 */
            font-size: 22px;
            font-weight: bold;
            background-color: #4a76a8;
            color: #ffffff;
            padding: 12px 24px;
            border-radius: 6px;
            letter-spacing: 3px;
            text-align: center;        /* 글자도 가운데 */
            width: fit-content;        /* 내용 크기만큼 박스 크기 */
            }

          .mail-footer {
            margin-top: 30px;
            padding: 15px 30px;
            font-size: 12px;
            color: #6b7280;
            background-color: #f9fafb;
            border-top: 1px solid #e5e7eb;
            text-align: center;
          }
          a {
            color: #4a76a8;
            text-decoration: none;
          }
        </style>
        </head>
        <body>
          <div class="mail-wrapper">
            <div class="mail-container">
              <div class="mail-header">
                오늘의빵 인증 메일
              </div>
              <div class="mail-body">
                안녕하세요, <span class="highlight">오늘의빵</span>를 이용해 주셔서 감사합니다.<br/>
                아래의 인증번호를 입력하시면 다음 단계로 진행할 수 있습니다.<br/>
                <div class="code-box">%s</div>
                <p>이 인증번호는 보안을 위해 일정 시간이 지나면 만료됩니다.<br/>
                문의사항이 있으시면 언제든지 <a href="https://192.168.0.27:8080/center">오늘의빵 고객센터</a>로 문의해 주세요.</p>
              </div>
              <div class="mail-footer">
                본 메일은 발신 전용입니다. 답장을 하셔도 회신되지 않습니다.<br/>
                &copy; 2025 오늘의빵. All rights reserved.
              </div>
            </div>
          </div>
        </body>
        </html>
        """
        .formatted(code);

    helper.setText(htmlContent, true);

    // [6] 발신자 지정
    helper.setFrom("projectermailsend@gmail.com");

    // [7] 메일 발송
    mailSender.send(message);

    return code;
  }

  // HTML 메일을 전송하는 메서드
  // 매개변수 : 수신자 이메일(to), 비밀번호(pw)
  public String sendPwMail(String to, String pw) throws MessagingException {
    // [1] MimeMessage 객체 생성
    // - HTML 메일, 첨부파일 등을 보낼 수 있는 고급 메시지 형식
    MimeMessage message = mailSender.createMimeMessage();

    // [2] MimeMessageHelper 생성
    // - (message, multipart여부, 인코딩)
    // - 두 번째 파라미터 true : 멀티파트(첨부파일) 허용
    // - 세 번째 파라미터 "UTF-8" : 한글 깨짐 방지
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // [3] 수신자 지정
    helper.setTo(to);

    // [4] 메일 제목 지정
    helper.setSubject("일회용 비밀번호");

    // [5] 메일 본문 지정
    // 두 번째 파라미터 true : 본문이 HTML 형식임을 지정
    String htmlContent = """
                                <!DOCTYPE html>
        <html lang="ko">
            <head>
                <meta charset="UTF-8" />
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: #f3f4f6;
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    }
                    .mail-wrapper {
                        width: 100%;
                        padding: 40px 0;
                        background-color: #f3f4f6;
                    }
                    .mail-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                        border: 1px solid #e5e7eb;
                    }
                    .mail-header {
                        background-color: #f46f40; /* 브랜드 주황 */
                        padding: 20px;
                        text-align: center;
                        color: #ffffff;
                        font-size: 24px;
                        font-weight: bold;
                    }
                    .mail-body {
                        padding: 30px;
                        font-size: 16px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .highlight {
                        font-weight: bold;
                        color: #f46f40; /* 브랜드 주황 */
                    }
                    .code-box {
                        display: block;
                        margin: 20px auto;
                        font-size: 22px;
                        font-weight: bold;
                        background-color: #f46f40; /* 브랜드 주황 */
                        color: #ffffff;
                        padding: 12px 24px;
                        border-radius: 6px;
                        letter-spacing: 3px;
                        text-align: center;
                        width: fit-content;
                    }
                    .mail-footer {
                        margin-top: 30px;
                        padding: 15px 30px;
                        font-size: 12px;
                        color: #6b7280;
                        background-color: #f9fafb;
                        border-top: 1px solid #e5e7eb;
                        text-align: center;
                    }
                    a {
                        color: #f46f40; /* 브랜드 주황 */
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="mail-wrapper">
                    <div class="mail-container">
                        <div class="mail-header">오늘의빵 보안 알림</div>
                        <div class="mail-body">
                            안녕하세요,
                            <span class="highlight">오늘의빵</span>
                            을 이용해 주셔서 감사합니다.
                            <br />
                            요청하신
                            <strong>일회용 비밀번호(OTP)</strong>
                            가 아래에 발급되었습니다.
                            <br />
                            로그인 또는 인증 화면에 아래 비밀번호를 입력해 주세요.
                            <br />

                            <div class="code-box">%s</div>

                            <p style="margin: 0">
                                ⚠️ 이 비밀번호는
                                <strong>한 번만 사용</strong>
                                할 수 있으며
                                <br />
                                보안을 위해
                                <strong>일정 시간이 지나면 자동으로 만료</strong>
                                됩니다.
                                <br />
                                <br />
                                문의사항이 있으시면 언제든지
                                <a href="https://192.168.0.27:8080/center">오늘의빵 고객센터</a>
                                로 문의해 주세요.
                            </p>
                        </div>
                        <div class="mail-footer">
                            본 메일은 발신 전용입니다. 답장을 하셔도 회신되지 않습니다.
                            <br />
                            &copy; 2025 millisec. All rights reserved.
                        </div>
                    </div>
                </div>
            </body>
        </html>
         """
        .formatted(pw);

    helper.setText(htmlContent, true);

    // [6] 발신자 지정
    helper.setFrom("projectermailsend@gmail.com");

    // [7] 메일 발송
    mailSender.send(message);

    return pw;
  }

  public String emailCode() {
    // 이메일 인증 코드 랜덤 생성
    // 숫자,대문자,소문자 사용해서 생성
    StringBuffer randomCode2 = new StringBuffer();
    Random r = new Random();
    for (int i = 0; i < 6; i++) {
      int flag = r.nextInt(3);// 0:숫자 1:대문자 2:소문자
      if (flag == 0) {
        int randNo = r.nextInt(10);
        randomCode2.append(randNo);
      } else if (flag == 1) {
        int randNo = r.nextInt(26) + 65;
        char ch = (char) randNo;
        randomCode2.append(ch);
      } else if (flag == 2) {
        int randNo = r.nextInt(26) + 97;
        char ch = (char) randNo;
        randomCode2.append(ch);
      }
    }
    return randomCode2.toString();
  }
}
