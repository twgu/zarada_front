"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

import axios from "axios";

import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

import Image from "next/image";
import img_joinProfile from "@/public/images/join-profile.png";

import { faPlusCircle } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export default function Fourth() {
  const router = useRouter();

  const [chNameVal, setChNameVal] = useState("");
  const [chNameError, setChNameError] = useState(false);
  const [chBirthDateVal, setChBirthDateVal] = useState("");
  const [chBirthDateError, setChBirthDateError] = useState(false);
  const [sexBoy, setSexBoy] = useState(true);
  const [sexGirl, setSexGirl] = useState(false);

  useEffect(() => {
    // 1.생년월일은 현재일 3년전 기본 세팅
    let date = new Date();
    let year = date.getFullYear() - 3;
    let month = date.getMonth() + 1;
    let day = date.getDate();

    if (month < 10) {
      month = "0" + month;
    }
    if (day < 10) {
      day = "0" + day;
    }

    let today = year + "-" + month + "-" + day;

    setChBirthDateVal(today);
  }, []);

  const fJoin = () => {
    if (chNameVal == "") {
      setChNameError(true);
      return;
    }

    if (!chNameError && !chBirthDateError) {
      router.push("/pages/join/membershipJoin/completion", { scroll: false });
    }
  };

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "chName":
        if (e.target.value == "") {
          setChNameError(true);
        } else {
          setChNameError(false);
        }
        setChNameVal(e.target.value);
        break;
      case "chBirthDate":
        if (e.target.value == "") {
          setChBirthDateError(true);
        } else {
          setChBirthDateError(false);
        }
        setChBirthDateVal(e.target.value);
        break;
    }
  };

  const onBlurHandler = (e, id) => {
    switch (id) {
      case "chName":
        if (chNameVal == "") {
          setChNameError(true);
        } else {
          setChNameError(false);
        }
        break;
      case "chBirthDate":
        if (chBirthDateVal == "") {
          setChBirthDateError(true);
        } else {
          setChBirthDateError(false);
        }
        break;
    }
  };

  const onClickHandler = (e, id) => {
    switch (id) {
      case "boy":
        setSexBoy(true);
        setSexGirl(false);
        break;
      case "girl":
        setSexBoy(false);
        setSexGirl(true);
        break;
      case "photo":
        alert("WEB 기능 미구현");
        break;
      case "join":
        fJoin();
        break;
    }
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="회원가입" />
      <div className="h-project-content join-membership agree-membership">
        <div className="loading-bar fourth"></div>
        <h3 className="join-tit">아이 출생정보를 적어주세요.</h3>
        <FormGroup error={chNameError} errorText="자녀이름을 입력해주세요.">
          <div className="input-area">
            <input
              type="num"
              className="default-input"
              id="agree-phone-number"
              placeholder="이름"
              value={chNameVal}
              onChange={(e) => {
                onChangeHandler(e, "chName");
              }}
              onBlur={(e) => {
                onBlurHandler(e, "chName");
              }}
            />
            <label htmlFor="agree-phone-number" className="blind">
              이름
            </label>
          </div>
        </FormGroup>
        <FormGroup
          error={chBirthDateError}
          errorText="자녀의 출생 년월일을 입력해주세요."
        >
          <div className="input-area">
            <input
              type="date"
              className="default-input"
              id="agree-phone-number"
              placeholder="생년월일"
              value={chBirthDateVal}
              onChange={(e) => {
                onChangeHandler(e, "chBirthDate");
              }}
              onBlur={(e) => {
                onBlurHandler(e, "chBirthDate");
              }}
            />
            <label htmlFor="agree-phone-number" className="blind">
              생년월일
            </label>
          </div>
        </FormGroup>
        <div className="input-area input-half-area">
          <button
            type="button"
            onClick={(e) => {
              onClickHandler(e, "boy");
            }}
            className={sexBoy ? "join-btn boy active" : "join-btn boy deactive"}
          >
            <span className="join-btn-txt">남자아이</span>
          </button>
          <button
            type="button"
            onClick={(e) => {
              onClickHandler(e, "girl");
            }}
            className={
              sexGirl ? "join-btn girl active" : "join-btn girl deactive"
            }
          >
            <span className="join-btn-txt">여자아이</span>
          </button>
        </div>
        <span className="birth-inform">아이 사진을 등록해주세요.</span>
        <label htmlFor="join-profile" className="blind">
          아이프로필
        </label>
        <button
          type="button"
          id="join-profile"
          onClick={(e) => {
            onClickHandler(e, "photo");
          }}
          className="profile-btn"
        >
          <span className="profile-area">
            <Image src={img_joinProfile} alt="profile" priority />
          </span>
          <FontAwesomeIcon
            icon={faPlusCircle}
            className="fas fa-plus-circle"
            style={{ background: "#fff" }}
          />
        </button>
        <div className="guide-txt">
          <p>· 그로트로핀-Ⅱ 를 처방받은 환자를 대상으로 하는 앱입니다.</p>
          <p>
            · 그로트로핀-Ⅱ 를 처방받은 환자 및 보호자 외에는 회원 가입이
            승인되지 않습니다.
          </p>
        </div>
        <div className="bottom-fixed">
          <button
            type="button"
            onClick={(e) => {
              onClickHandler(e, "join");
            }}
            className="default-block-btn agree-next-btn"
          >
            가입하기
          </button>
        </div>
      </div>
    </div>
  );
}
