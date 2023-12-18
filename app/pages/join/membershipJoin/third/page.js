"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

const idReg = /^[a-zA-Z0-9]{4,12}$/; //4~12자의 영문 대소문자와 숫자 조합
const pwReg = /^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,16}/; //6-16자리 영문, 숫자, 특수문자 조합

export default function Third() {
  const router = useRouter();

  const [idChk, setIdChk] = useState(false);

  const [idError, setIdError] = useState(false);
  const [idVal, setIdVal] = useState("");
  const [pwError, setPwError] = useState(false);
  const [pwErrorMsg, setPwErrorMsg] = useState("");
  const [pwVal, setPwVal] = useState("");
  const [pwOkError, setPwOkError] = useState(false);
  const [pwOkErrorMsg, setPwOkErrorMsg] = useState("");
  const [pwOkVal, setPwOkVal] = useState("");

  const fIdCheck = () => {
    if (idError) {
      alert("아이디를 입력 해주세요.");
      return;
    }

    if (!idReg.test(idVal)) {
      alert("아이디는 4~12자의 영문 대소문자와 숫자로만 입력해야합니다.");
      return;
    }

    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/Idcheck", {
        id: idVal,
      })
      .then((response) => {
        if (response.data.idcount > 0) {
          setIdChk(false);
          alert("중복된 아이디입니다. 다른 아이디를 선택해주세요.");
        } else {
          setIdChk(true);
          alert("사용 가능한 아이디입니다.");
        }
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  const fNextStep = () => {
    if (!idChk) {
      alert("아이디 중복확인 후 진행하시기 바랍니다.");
      return;
    }

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

    localStorage.setItem("join_userId", idVal);
    localStorage.setItem("join_password", pwVal);
    localStorage.setItem("join_repeatPassword", pwOkVal);

    router.push("/pages/join/membershipJoin/fourth", {
      scroll: false,
    });
  };

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "id":
        setIdChk(false);
        if (e.target.value == "") {
          setIdError(true);
        } else {
          setIdError(false);
        }
        setIdVal(e.target.value);
        break;
      case "pw":
        if (e.target.value == "") {
          setPwError(true);
          setPwErrorMsg("비밀번호 필드를 입력해주세요.");
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
          setPwOkErrorMsg("비밀번호 확인 필드를 입력해주세요.");
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
      case "id":
        if (idVal == "") {
          setIdError(true);
        }
        break;
      case "pw":
        if (pwVal == "") {
          setPwError(true);
          setPwErrorMsg("비밀번호 필드를 입력해주세요.");
        }
        break;
      case "pwOk":
        if (pwOkVal == "") {
          setPwOkError(true);
          setPwOkErrorMsg("비밀번호 확인 필드를 입력해주세요.");
        }
        break;
    }
  };

  const onClickHandler = (id) => {
    switch (id) {
      case "idCheck":
        fIdCheck();
        break;
      case "next":
        fNextStep();
        break;
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="회원가입" />
      <div className="h-project-content join-membership agree-membership">
        <div className="loading-bar third"></div>
        <h3 className="join-tit">아이디와 비밀번호를 적어주세요.</h3>
        <FormGroup error={idError} errorText="아이디를 입력해주세요.">
          <div className="input-area calc-input-area">
            <input
              type="text"
              className="default-input"
              id="agree-phone-number"
              placeholder="아이디"
              value={idVal}
              onChange={(e) => {
                onChangeHandler(e, "id");
              }}
              onBlur={(e) => {
                onBlurHandler(e, "id");
              }}
            />
            <label htmlFor="agree-phone-number" className="blind">
              아이디
            </label>
            <button
              type="button"
              className="agree-right-btn"
              onClick={() => {
                onClickHandler("idCheck");
              }}
            >
              중복확인
            </button>
          </div>
        </FormGroup>
        <FormGroup error={pwError} errorText={pwErrorMsg}>
          <div className="input-area">
            <label className="blind" htmlFor="join-pwd">
              비밀번호
            </label>
            <input
              className="default-input"
              id="join-pwd"
              type="password"
              placeholder="비밀번호"
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
            <label className="blind" htmlFor="join-pwd-chk">
              비밀번호 재입력
            </label>
            <input
              className="default-input"
              id="join-pwd-chk"
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
            className="default-block-btn agree-next-btn"
            onClick={() => {
              onClickHandler("next");
            }}
          >
            다 음
          </button>
        </div>
      </div>
    </div>
  );
}
