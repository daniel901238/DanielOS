using ProjectRelay.UI;
using UnityEngine;

namespace ProjectRelay.Player
{
    public class BalanceAssistController : MonoBehaviour
    {
        [SerializeField] private HoldButtonInput holdButton;
        [SerializeField] private KeyCode keyboardFallbackKey = KeyCode.Space;
        [SerializeField, Range(0f, 1f)] private float slipChanceReduction = 0.45f;

        public bool IsActive
        {
            get
            {
                bool mobileHold = holdButton != null && holdButton.IsHolding;
                bool keyHold = Input.GetKey(keyboardFallbackKey);
                return mobileHold || keyHold;
            }
        }

        public float GetSlipChanceMultiplier()
        {
            return IsActive ? Mathf.Clamp01(1f - slipChanceReduction) : 1f;
        }

        public float GetStaminaDrainMultiplier()
        {
            return IsActive ? Mathf.Max(1f, staminaDrainMultiplierWhenActive) : 1f;
        }
    }
}
