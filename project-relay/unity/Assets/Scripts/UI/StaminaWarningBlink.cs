using ProjectRelay.Player;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class StaminaWarningBlink : MonoBehaviour
    {
        [SerializeField] private PlayerLocomotion playerLocomotion;
        [SerializeField] private Graphic targetGraphic;
        [SerializeField, Range(0f, 1f)] private float warningThreshold = 0.25f;
        [SerializeField] private float blinkSpeed = 8f;
        [SerializeField] private Color normalColor = Color.white;
        [SerializeField] private Color warningColor = new Color(1f, 0.25f, 0.25f, 1f);

        private void Awake()
        {
            if (playerLocomotion == null)
                playerLocomotion = FindObjectOfType<PlayerLocomotion>();
        }

        private void Update()
        {
            if (playerLocomotion == null || targetGraphic == null) return;

            float stamina = playerLocomotion.StaminaNormalized;
            if (stamina <= warningThreshold)
            {
                float t = (Mathf.Sin(Time.time * blinkSpeed) + 1f) * 0.5f;
                targetGraphic.color = Color.Lerp(normalColor, warningColor, t);
            }
            else
            {
                targetGraphic.color = normalColor;
            }
        }
    }
}
