"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

export default function MembershipPwFind() {
  const router = useRouter();

  const [idError, setIdError] = useState(false);
  const [idVal, setIdVal] = useState("");
  const [phoneVal, setPhoneVal] = useState("");
  const [nameVal, setNameVal] = useState("");

  const fPwFind = () => {
    if (idVal == "") {
      alert("아이디를 입력해 주시기 바랍니다.");
      return;
    }

    if (phoneVal == "" || nameVal == "") {
      alert("휴대폰인증 후 진행하시기 바랍니다.");
      return;
    }

    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/userIdcheck", {
        findPwId: idVal,
        userPhone: phoneVal,
        userName: nameVal,
      })
      .then((response) => {
        if (response.data.idcount > 0) {
          localStorage.setItem("findUserCo", response.data.userCo);
          router.push("/pages/join/membershipPwFind/success", {
            scroll: false,
          });
        } else {
          alert("등록된 회원정보가 없습니다. 입력정보를 다시 확인해주세요.");
        }
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "id":
        if (e.target.value == "") {
          setIdError(true);
        } else {
          setIdError(false);
        }
        setIdVal(e.target.value);
        break;
    }
  };

  const onBlurHandler = (e, id) => {
    switch (id) {
      case "id":
        if (idVal == "") {
          setIdError(true);
        }
        break;
    }
  };

  const onClickHandler = (id) => {
    switch (id) {
      case "nicePhone":
        // TODO : NICE 인증
        alert("나이스 휴대폰 인증 모듈 호출");
        setPhoneVal("01023089903");
        setNameVal("구태완");
        break;
      case "pwFind":
        fPwFind();
        break;
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="비밀번호 찾기" />
      <div className="h-project-content join-membership">
        <h3 className="join-tit">아이디 입력 후 휴대폰 인증을 진행하세요</h3>
        <div className="tab-template">
          <FormGroup error={idError} errorText="아이디를 입력해주세요.">
            <div className="input-area">
              <input
                type="text"
                id="find-pw-id"
                placeholder="아이디"
                className="default-input"
                value={idVal}
                onChange={(e) => {
                  onChangeHandler(e, "id");
                }}
                onBlur={(e) => {
                  onBlurHandler(e, "id");
                }}
              />
              <label htmlFor="find-pw-id" className="blind">
                아이디
              </label>
            </div>
          </FormGroup>
          <div className="form-group">
            <div className="input-area calc-input-area">
              <input
                type="num"
                className="default-input"
                id="agree-phone-number"
                placeholder="휴대폰 번호"
                value={phoneVal}
                disabled
              />
              <label htmlFor="agree-phone-number" className="blind">
                휴대폰 번호
              </label>
              <button
                type="button"
                className="agree-right-btn"
                onClick={() => {
                  onClickHandler("nicePhone");
                }}
              >
                인증
              </button>
            </div>
          </div>
          <div className="form-group">
            <div className="input-area">
              <input
                type="text"
                className="default-input"
                id="find-pw-name"
                placeholder="보호자 이름"
                value={nameVal}
                disabled
              />
              <label htmlFor="find-pw-name" className="blind">
                보호자 이름
              </label>
            </div>
          </div>
          <div className="bottom-fixed">
            <button
              type="button"
              className="default-block-btn"
              onClick={() => {
                onClickHandler("pwFind");
              }}
            >
              비밀번호 찾기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
