"use client";

import { useState } from "react";

export default function ValidationInput(props) {
  const [error, setError] = useState(false);
  const [val, setVal] = useState("");

  const change = (e) => {
    if (e.target.value == "") {
      setError(true);
    } else {
      setError(false);
    }

    setVal(e.target.value);
  };

  const blur = () => {
    if (val == "") {
      setError(true);
    }
  };

  return (
    <div className={error ? "form-group error" : "form-group valid"}>
      <div className="input-area">
        <input
          className="default-input"
          id={props.id}
          type={props.type}
          placeholder={props.placeholder}
          value={val}
          onChange={change}
          onBlur={blur}
        />
        <label htmlFor={props.id} className="blind">
          {props.placeholder}
        </label>
      </div>
      {error ? (
        <div className="form-error is-visible">
          <div
            data-validation-attr={`${props.placeholder}를(을) 입력해주세요.`}
          >
            {props.placeholder}를(을) 입력해주세요.
          </div>
        </div>
      ) : null}
    </div>
  );
}
