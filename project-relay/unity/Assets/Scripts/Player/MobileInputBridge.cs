using UnityEngine;

namespace ProjectRelay.Player
{
    public class MobileInputBridge : MonoBehaviour
    {
        [Header("References")]
        [SerializeField] private JoystickLikeInput joystick;
        [SerializeField] private bool oneHandAutoForward = false;
        [SerializeField] private float autoForwardValue = 0.65f;

        public Vector2 MoveInput { get; private set; }

        private void Update()
        {
            if (joystick != null)
            {
                MoveInput = joystick.InputVector;
            }
            else
            {
                // 키보드 fallback
                MoveInput = new Vector2(Input.GetAxisRaw("Horizontal"), Input.GetAxisRaw("Vertical"));
            }

            if (oneHandAutoForward)
            {
                MoveInput = new Vector2(MoveInput.x, Mathf.Max(MoveInput.y, autoForwardValue));
            }

            MoveInput = Vector2.ClampMagnitude(MoveInput, 1f);
        }
    }
}
