"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

const pwReg = /^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,16}/; //6-16자리 영문, 숫자, 특수문자 조합

export default function Success() {
  const router = useRouter();

  const [pwError, setPwError] = useState(false);
  const [pwErrorMsg, setPwErrorMsg] = useState("");
  const [pwVal, setPwVal] = useState("");
  const [pwOkError, setPwOkError] = useState(false);
  const [pwOkErrorMsg, setPwOkErrorMsg] = useState("");
  const [pwOkVal, setPwOkVal] = useState("");

  const fPwChange = () => {
    if (pwError || pwOkError) {
      alert("비밀번호와 비밀번호 확인 필드 모두 올바른 값을 입력 해주세요.");
      return;
    }
    if (!pwReg.test(pwOkVal)) {
      alert(
        "비밀번호는 6-16자의 영문, 숫자, 특수문자 조합으로 입력해야합니다."
      );
      return;
    }

    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/updatePwd", {
        user_co: localStorage.getItem("findUserCo"),
        newpwd: pwOkVal,
      })
      .then((response) => {
        alert("비밀번호 변경이완료되었습니다.");
        router.push("/pages/login", {
          scroll: false,
        });
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "pw":
        if (e.target.value == "") {
          setPwError(true);
          setPwErrorMsg("비밀번호 재설정 필드를 입력해주세요.");
        } else {
          if (e.target.value.length < 6) {
            setPwError(true);
            setPwErrorMsg("최소 6자 이상 입력해주세요.");
          } else {
            setPwError(false);
          }
        }
        if (pwOkVal.length > 0) {
          if (e.target.value != pwOkVal) {
            setPwOkError(true);
            setPwOkErrorMsg("비밀번호가 일치하지 않습니다.");
          } else {
            setPwOkError(false);
          }
        }
        setPwVal(e.target.value);
        break;
      case "pwOk":
        if (e.target.value == "") {
          setPwOkError(true);
          setPwOkErrorMsg("비밀번호 재설정 확인 필드를 입력해주세요.");
        } else {
          if (e.target.value != pwVal) {
            setPwOkError(true);
            setPwOkErrorMsg("비밀번호가 일치하지 않습니다.");
          } else {
            setPwOkError(false);
          }
        }
        setPwOkVal(e.target.value);
        break;
    }
  };

  const onBlurHandler = (e, id) => {
    switch (id) {
      case "pw":
        if (pwVal == "") {
          setPwError(true);
          setPwErrorMsg("비밀번호 재설정 필드를 입력해주세요.");
        }
        break;
      case "pwOk":
        if (pwOkVal == "") {
          setPwOkError(true);
          setPwOkErrorMsg("비밀번호 재설정 확인 필드를 입력해주세요.");
        }
        break;
    }
  };

  const onClickHandler = (id) => {
    switch (id) {
      case "pwChange":
        fPwChange();
        break;
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="비밀번호 찾기" />
      <div className="h-project-content join-membership">
        <h3 className="join-tit">비밀번호를 재설정 해주세요</h3>
        <div className="tab-template">
          <FormGroup error={pwError} errorText={pwErrorMsg}>
            <div className="input-area">
              <label className="blind" htmlFor="pw-reset">
                비밀번호
              </label>
              <input
                className="default-input"
                type="password"
                placeholder="비밀번호를 입력해주세요"
                value={pwVal}
                onChange={(e) => {
                  onChangeHandler(e, "pw");
                }}
                onBlur={(e) => {
                  onBlurHandler(e, "pw");
                }}
              />
            </div>
          </FormGroup>
          <FormGroup error={pwOkError} errorText={pwOkErrorMsg}>
            <div className="input-area">
              <label className="blind" htmlFor="pw-reset-again">
                비밀번호 재입력
              </label>
              <input
                className="default-input"
                type="password"
                placeholder="비밀번호 확인"
                value={pwOkVal}
                onChange={(e) => {
                  onChangeHandler(e, "pwOk");
                }}
                onBlur={(e) => {
                  onBlurHandler(e, "pwOk");
                }}
              />
            </div>
          </FormGroup>
          <div className="bottom-fixed">
            <button
              type="button"
              className="default-block-btn"
              onClick={() => {
                onClickHandler("pwChange");
              }}
            >
              비밀번호 재설정
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
