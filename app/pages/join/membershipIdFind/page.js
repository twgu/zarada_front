"use client";

import axios from "axios";
import { useState } from "react";
import { useRouter } from "next/navigation";

import MembershipHeader from "@/app/components/MembershipHeader";
import FormGroup from "@/app/components/FormGroup";

export default function MembershipIdFind() {
  const router = useRouter();

  const [nameError, setNameError] = useState(false);
  const [nameVal, setNameVal] = useState("");
  const [phoneError, setPhoneError] = useState(false);
  const [phoneVal, setPhoneVal] = useState("");

  const onChangeHandler = (e, id) => {
    switch (id) {
      case "name":
        if (e.target.value == "") {
          setNameError(true);
        } else {
          setNameError(false);
        }
        setNameVal(e.target.value);
        break;
      case "phone":
        if (e.target.value == "") {
          setPhoneError(true);
        } else {
          setPhoneError(false);
        }
        setPhoneVal(e.target.value);
        break;
    }
  };

  const onBlurHandler = (e, id) => {
    switch (id) {
      case "name":
        if (nameVal == "") {
          setNameError(true);
        } else {
          setNameError(false);
        }
        break;
      case "phone":
        if (phoneVal == "") {
          setPhoneError(true);
        } else {
          setPhoneError(false);
        }
        break;
    }
  };

  const onClickHandler = () => {
    axios
      .post(process.env.NEXT_PUBLIC_API_URL + "/FindId", {
        user_co: "",
        deviceType: "0",
        user_Na: nameVal,
        hp: phoneVal,
      })
      .then((response) => {
        if (response.data == "") {
          router.push("/pages/join/membershipIdFind/fail", { scroll: false });
        } else {
          localStorage.setItem("findId", response.data.id);
          router.push("/pages/join/membershipIdFind/success", {
            scroll: false,
          });
        }
      })
      .catch((error) => {
        alert(error.message);
      });
  };

  return (
    <div className="h-project-template">
      <MembershipHeader title="아이디 찾기" />
      <div className="h-project-content join-membership">
        <h3 className="join-tit">보호자 이름, 휴대폰 번호를 적어주세요</h3>
        <div className="tab-template">
          <FormGroup error={nameError} errorText="보호자 이름을 입력해주세요.">
            <div className="input-area">
              <input
                className="default-input"
                id="find-id-name"
                type="text"
                placeholder="보호자 이름"
                value={nameVal}
                onChange={(e) => {
                  onChangeHandler(e, "name");
                }}
                onBlur={(e) => {
                  onBlurHandler(e, "name");
                }}
              />
              <label htmlFor="find-id-name" className="blind">
                보호자 이름
              </label>
            </div>
          </FormGroup>
          <FormGroup error={phoneError} errorText="휴대폰 번호를 입력해주세요.">
            <div className="input-area">
              <input
                className="default-input"
                id="find-id-phone"
                type="text"
                placeholder="휴대폰 번호"
                value={phoneVal}
                onChange={(e) => {
                  onChangeHandler(e, "phone");
                }}
                onBlur={(e) => {
                  onBlurHandler(e, "phone");
                }}
              />
              <label htmlFor="find-id-phone" className="blind">
                휴대폰 번호
              </label>
            </div>
          </FormGroup>
          <div className="bottom-fixed">
            <button
              type="button"
              className="default-block-btn"
              onClick={onClickHandler}
            >
              아이디 찾기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
