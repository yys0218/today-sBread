package com.ex.center.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ex.center.model.service.SettleService;
import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/settle")
public class SettleController {

    private final SettleService settleService;

    /**
     * 정산하는 경로
     * @Param int memberNo
     * url : /center/settle/apply
     * 정산 신청하면 잔액(Balance) 값을 0 으로 초기화, 같은 값 만큼 유형 3으로 입금 
     * Return success(성공) / fail(실패) / zero(잔액이 0원) / error(오류)
     */
    @PostMapping("/apply")
    @ResponseBody
    public String settleApply(@RequestParam(value="memberNo", required=false) Integer memberNo, HttpServletRequest request) {
        // if(memberNo == null){
        //     MemberDTO member = (MemberDTO) request.getSession().getAttribute("user");
        //     memberNo = member.getMemberNo();
        // }
        
        String result = this.settleService.apply(memberNo);
        return result;
    }
}
