using ProjectRelay.Player;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class StaminaBarUI : MonoBehaviour
    {
        [SerializeField] private PlayerLocomotion playerLocomotion;
        [SerializeField] private Slider staminaSlider;
        [SerializeField] private bool hideWhenFull = false;

        private void Update()
        {
            if (playerLocomotion == null || staminaSlider == null) return;

            float value = playerLocomotion.StaminaNormalized;
            staminaSlider.value = value;

            if (hideWhenFull)
                staminaSlider.gameObject.SetActive(value < 0.999f);
        }
    }
}
